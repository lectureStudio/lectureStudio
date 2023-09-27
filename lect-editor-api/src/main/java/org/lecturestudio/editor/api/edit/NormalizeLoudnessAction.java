package org.lecturestudio.editor.api.edit;

import java.io.File;
import java.io.IOException;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.recording.RecordedAudio;
import org.lecturestudio.core.recording.Recording;
import org.lecturestudio.core.recording.Recording.Content;
import org.lecturestudio.core.recording.RecordingEditException;
import org.lecturestudio.core.recording.edit.RecordedObjectAction;
import org.lecturestudio.core.util.ProgressCallback;
import org.lecturestudio.media.audio.FFmpegLoudnessNormalization;
import org.lecturestudio.media.audio.LoudnessConfiguration;

/**
 * Normalizes the audio loudness (perceived volume) of the audio track
 */
public class NormalizeLoudnessAction extends RecordedObjectAction<RecordedAudio> {

    private final Recording recording;
    private final Double lufsValue;
    private final ProgressCallback callback;
    private RandomAccessAudioStream oldStream;
    private RandomAccessAudioStream newStream;


    public NormalizeLoudnessAction(Recording recording, Double lufsValue, ProgressCallback callback) {
        super(recording.getRecordedAudio());
        this.recording = recording;
        this.lufsValue = lufsValue;
        this.callback = callback;
    }

    @Override
    public void undo() throws RecordingEditException {
        try {
            getRecordedObject().setAudioStream(oldStream);
            recording.fireChangeEvent(Content.AUDIO);
        }
        catch (IOException e) {
            throw new RecordingEditException(e);
        }
    }

    @Override
    public void redo() throws RecordingEditException {
        try {
            getRecordedObject().setAudioStream(newStream);
            recording.fireChangeEvent(Content.AUDIO);
        }
        catch (IOException e) {
            throw new RecordingEditException(e);
        }
    }

    @Override
    public void execute() throws RecordingEditException {
        LoudnessConfiguration configuration;
        RandomAccessAudioStream stream = getRecordedObject().getAudioStream();
        oldStream = stream.clone();

        try {
            int totalSteps = 2;
            int currentStep = 0;

            FFmpegLoudnessNormalization normalization = new FFmpegLoudnessNormalization();
            int finalCurrentStep = currentStep;
            configuration = normalization.retrieveInformation(stream,
                    (progress) -> callback.onProgress((progress + finalCurrentStep) / totalSteps));
            callback.onProgress((float) ++currentStep / totalSteps);

            LoudnessConfiguration targetConfiguration = new LoudnessConfiguration(configuration);
            targetConfiguration.setLUFSValue(lufsValue);

            int finalCurrentStep1 = currentStep;
            File newInstreamFile = normalization.normalize(stream, configuration, targetConfiguration,
                    (progress) -> callback.onProgress((progress + finalCurrentStep1) / totalSteps));
            stream.close();

            getRecordedObject().setAudioStream(new RandomAccessAudioStream(newInstreamFile));

            callback.onProgress((float) ++currentStep / totalSteps);
            newStream = new RandomAccessAudioStream(newInstreamFile);
            recording.fireChangeEvent(Content.AUDIO);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
