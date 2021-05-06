/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.audio.effect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.lecturestudio.core.ExecutableBase;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.core.ProgressListener;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.audio.AudioUtils;
import org.lecturestudio.core.audio.sink.AudioSink;
import org.lecturestudio.core.audio.source.AudioSource;
import org.lecturestudio.core.bus.event.ProgressEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base audio effect implementation to provide a consistent mechanism to process
 * audio samples.
 *
 * @author Alex Andres
 */
public abstract class AudioEffectRunner extends ExecutableBase {

	/** Logger for {@link AudioEffectRunner} */
	private static final Logger LOG = LogManager.getLogger(AudioEffectRunner.class);

	/** The audio effect parameters. */
	protected final AudioEffectParameters params;

	/** The audio source from which to read the audio samples. */
	protected final AudioSource source;

	/** The audio sink to which to write the processed audio samples. */
	protected final AudioSink sink;

	/** Registered effect state listeners. */
	private final List<ProgressListener> listeners;

	/** Indicates whether the executor is rendering audio. */
	private final AtomicBoolean render = new AtomicBoolean(false);

	/** The effect rendering thread. */
	private Thread renderThread;


	/**
	 * Start audio effect processing.
	 *
	 * @throws IOException If an fatal error occurred preventing the audio
	 *                     effect producing output samples.
	 */
	abstract protected void runEffect() throws IOException;


	/**
	 * Create an {@link AudioEffectRunner} with the desired output parameters and the
	 * audio source and sink.
	 *
	 * @param params The audio output parameters.
	 * @param source The audio source from which to read the audio samples.
	 * @param sink   The audio sink to which to write the processed audio
	 *               samples.
	 */
	public AudioEffectRunner(AudioEffectParameters params, AudioSource source, AudioSink sink) {
		this.params = params;
		this.source = source;
		this.sink = sink;
		this.listeners = new ArrayList<>();
	}

	/**
	 * Register an listener to receive effect progress events.
	 *
	 * @param listener The listener to add.
	 */
	public void addProgressListener(ProgressListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove an progress listener from the listener list.
	 *
	 * @param listener The listener to remove.
	 */
	public void removeListener(ProgressListener listener) {
		listeners.remove(listener);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		try {
			sink.open();
		}
		catch (IOException e) {
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		render.set(true);

		renderThread = new Thread(() -> {
			try {
				runEffect();
				close();
				fireFinished();
			}
			catch (Exception e) {
				LOG.error("Run audio effect failed.", e);
			}
		}, getClass().getSimpleName());

		renderThread.start();
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		render.set(false);

		// Stop gracefully.
		if (renderThread.isAlive()) {
			try {
				renderThread.join();
			}
			catch (InterruptedException e) {
				throw new ExecutableException(e);
			}
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		renderThread = null;
	}

	/**
	 * Check whether the executor is rendering audio.
	 *
	 * @return true if the executor is rendering audio, false otherwise.
	 */
	protected boolean isRendering() {
		return render.get();
	}

	/**
	 * Close the audio source and sink.
	 *
	 * @throws IOException If the audio source or the audio sink failed to close.
	 */
	protected void close() throws IOException {
		source.close();
		sink.close();
	}

	/**
	 * Notify the listeners that audio rendering is complete.
	 */
	protected void fireFinished() {
		if (!render.get()) {
			return;
		}

		LOG.debug("Setting state for [{}] to [{}]", this, "Finished");

		for (ProgressListener listener : listeners) {
			listener.onProgress(new ProgressEvent(1));
		}
	}

	/**
	 * Notify that the {@link AudioEffect} has made progress while processing. The
	 * progress value must be in the range of [0,1].
	 *
	 * @param sink     The audio sink to which to write the processed audio
	 *                 samples.
	 * @param progress The progress of the effect rendering.
	 */
	protected void fireProgress(AudioSink sink, float progress) {
		if (sink == null) {
			return;
		}

		for (ProgressListener listener : listeners) {
			listener.onProgress(new ProgressEvent(progress));
		}
	}

	/**
	 * Execute the provided effect with the audio output parameters and write
	 * the result into the provided audio sink.
	 *
	 * @param effect The effect to execute.
	 * @param params The audio output parameters.
	 * @param sink   The audio sink to which to write the processed audio
	 *               samples.
	 *
	 * @throws IOException If a fatal error occurred preventing the audio
	 *                     effect producing output samples.
	 */
	protected void runEffect(AudioEffect effect, AudioEffectParameters params, AudioSink sink) throws IOException {
		AudioFormat audioFormat = source.getAudioFormat();

		int channels = audioFormat.getChannels();
		int sampleSize = audioFormat.getBytesPerSample();
		long length = source.getInputSize();

		// Skip audio RIFF-WAVE header.
		source.reset();
		source.skip(44);

		// IMPORTANT: Read chunks of 1024 samples due to effect window size.
		int bufferSize = sampleSize * 1024;

		effect.initialize(params);

		Samples inputSamples;
		Samples outputSamples;

		byte[] buffer = new byte[bufferSize];

		long audioRead = 0;
		long read;

		while (render.get() && (read = source.read(buffer, 0, bufferSize)) > 0) {
			int sampleCount = (int) (read / (sampleSize * channels));
			float[] inSamples = new float[sampleCount];
			float[] outSamples = new float[sampleCount * 4];
			int x = 0;

			if (read != bufferSize) {
				read--;
			}

			for (int i = 0; i < read; i += sampleSize) {
				int sample = AudioUtils.toShort(new byte[] { buffer[i], buffer[i + 1] }, false);
				inSamples[x++] = AudioUtils.getNormalizedSampleValue(sample, sampleSize, true);
			}

			inputSamples = new Samples(audioFormat, inSamples, inSamples.length);
			outputSamples = new Samples(audioFormat, outSamples, outSamples.length);

			effect.execute(inputSamples, outputSamples);

			writeSamples(sink, outputSamples);

			// Update state progress.
			audioRead += read;

			fireProgress(sink, 1.f * audioRead / length);
		}

		// Flush, if not terminated.
		if (render.get()) {
			float[] outSamples = new float[1024 * 2];
			outputSamples = new Samples(audioFormat, outSamples, outSamples.length);

			effect.flush(outputSamples);

			writeSamples(sink, outputSamples);

			effect.terminate();
		}
	}

	private void writeSamples(AudioSink sink, Samples outputSamples) throws IOException {
		if (sink == null) {
			return;
		}

		int out = outputSamples.getSampleCount();

		if (out > 0) {
			float[] samples = outputSamples.getSamples();
			byte[] buffer = new byte[out * 2];

			for (int i = 0, j = 0; i < out; i++, j += 2) {
				int sample = AudioUtils.toShort(samples[i]);

				buffer[j] = (byte) (sample & 0xFF);
				buffer[j + 1] = (byte) ((sample >> 8) & 0xFF);
			}

			sink.write(buffer, 0, buffer.length);
		}
	}

}
