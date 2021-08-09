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

package org.lecturestudio.editor.api.presenter;

import com.google.common.eventbus.Subscribe;
import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.audio.AudioFormat;
import org.lecturestudio.core.beans.ObjectProperty;
import org.lecturestudio.core.bus.event.DocumentEvent;
import org.lecturestudio.core.codec.CodecID;
import org.lecturestudio.core.geometry.Dimension2D;
import org.lecturestudio.core.geometry.Rectangle2D;
import org.lecturestudio.core.model.Document;
import org.lecturestudio.core.model.Page;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.core.presenter.command.ShowPresenterCommand;
import org.lecturestudio.core.service.DocumentService;
import org.lecturestudio.core.util.AudioUtils;
import org.lecturestudio.editor.api.context.EditorContext;
import org.lecturestudio.editor.api.view.VideoExportSettingsView;
import org.lecturestudio.media.config.AudioRenderConfiguration;
import org.lecturestudio.media.config.RenderConfiguration;
import org.lecturestudio.media.config.VideoRenderConfiguration;
import org.lecturestudio.media.video.AVDefaults;
import org.lecturestudio.media.video.VideoFormat;

import javax.inject.Inject;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class VideoExportSettingsPresenter extends Presenter<VideoExportSettingsView> {

	private final RenderConfiguration renderConfig;

	private final DocumentService documentService;

	private ObjectProperty<VideoFormat> videoFormatProperty;


	@Inject
	VideoExportSettingsPresenter(ApplicationContext context, VideoExportSettingsView view,
			DocumentService documentService) {
		super(context, view);

		this.renderConfig = ((EditorContext) context).getRenderConfiguration();
		this.documentService = documentService;
	}

	@Override
	public void initialize() {
		VideoFormat videoFormat = AVDefaults.VIDEO_FORMATS[0];
		AudioFormat[] audioFormats = getAudioFormats(videoFormat);
		Dimension2D[] dimensions = getDimensions();

		AudioRenderConfiguration audioRenderConfig = renderConfig.getAudioConfig();
		VideoRenderConfiguration videoRenderConfig = renderConfig.getVideoConfig();

		audioRenderConfig.setBitrate(AVDefaults.AUDIO_BITRATES[3]);
		audioRenderConfig.setCodecID(videoFormat.getAudioCodecID());
		audioRenderConfig.setOutputFormat(AudioUtils.getNearestFormat(audioFormats, 22050));
		audioRenderConfig.setVBR(false);

		videoRenderConfig.setBitrate(32);
		videoRenderConfig.setCodecID(videoFormat.getVideoCodecID());
		videoRenderConfig.setFrameRate(AVDefaults.FRAME_RATES[1]);
		videoRenderConfig.setTwoPass(false);

		renderConfig.setAudioConfig(audioRenderConfig);
		renderConfig.setVideoConfig(videoRenderConfig);
		renderConfig.setFileFormat(videoFormat.getOutputFormat());

		videoFormatProperty = new ObjectProperty<>(videoFormat);
		videoFormatProperty.addListener((observable, oldValue, newValue) -> {
			AudioFormat[] newAudioFormats = getAudioFormats(newValue);

			view.setAudioFormats(newAudioFormats);

			audioRenderConfig.setOutputFormat(AudioUtils.getNearestFormat(newAudioFormats, 24000));
			audioRenderConfig.setCodecID(newValue.getAudioCodecID());
			videoRenderConfig.setCodecID(newValue.getVideoCodecID());

			renderConfig.setFileFormat(newValue.getOutputFormat());
		});

		view.setAudioBitrates(AVDefaults.AUDIO_BITRATES);
		view.bindAudioBitrate(audioRenderConfig.bitrateProperty());
		view.setAudioFormats(audioFormats);
		view.bindAudioFormat(audioRenderConfig.outputFormatProperty());
		view.bindAudioVBR(audioRenderConfig.vbrProperty());
		view.bindTwoPassEncoding(videoRenderConfig.twoPassProperty());
		view.bindDimension(videoRenderConfig.dimensionProperty());
		view.setFrameRates(AVDefaults.FRAME_RATES);
		view.bindFrameRate(videoRenderConfig.frameRateProperty());
		view.bindVideoBitrate(videoRenderConfig.bitrateProperty());
		view.setVideoFormats(AVDefaults.VIDEO_FORMATS);
		view.bindVideoFormat(videoFormatProperty);
		view.setOnCreate(this::create);

		if (nonNull(dimensions)) {
			videoRenderConfig.setDimension(dimensions[3]);

			view.setDimensions(dimensions);
		}

		context.getEventBus().register(this);
	}

	@Override
	public void destroy() {
		context.getEventBus().unregister(this);
	}

	@Subscribe
	public void onEvent(DocumentEvent event) {
		if (event.created() || event.selected()) {
			Dimension2D[] dimensions = getDimensions();

			if (nonNull(dimensions)) {
				view.setDimensions(dimensions);
				view.setDimension(dimensions[3]);
				renderConfig.getVideoConfig().setDimension(dimensions[3]);
			}
		}
	}

	private void create() {
		context.getEventBus().post(new ShowPresenterCommand<>(VideoExportPresenter.class));
	}

	private Dimension2D[] getDimensions() {
		Document selectedDoc = documentService.getDocuments().getSelectedDocument();

		if (isNull(selectedDoc)) {
			return null;
		}

		Page page = selectedDoc.getPage(0);
		Rectangle2D pageRect = page.getPageRect();

//		if (selectedDoc.isScreenCapture()) {
//			pageRect = selectedDoc.getScreenCaptureDocument().getPageRect(0);
//		} else {
//			pageRect = page.getPageRect();
//		}

		double width = pageRect.getWidth();
		double height = pageRect.getHeight();

		return AVDefaults.getDimensions(width, height);
	}

	private AudioFormat[] getAudioFormats(VideoFormat profile) {
		Integer[] sampleRates;

		if (profile.getAudioCodecID() == CodecID.OPUS) {
			sampleRates = AVDefaults.OPUS_SAMPLE_RATES;
		}
		else {
			sampleRates = AVDefaults.SAMPLE_RATES;
		}

		AudioFormat[] array = new AudioFormat[sampleRates.length];

		for (int i = 0; i < sampleRates.length; i++) {
			array[i] = new AudioFormat(AudioFormat.Encoding.S16LE, sampleRates[i], 1);
		}

		return array;
	}
}
