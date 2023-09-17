package org.lecturestudio.media.audio;

import java.util.Locale;

/**
 * This is the loudness configuration provided and required by the ffmpeg loudnorm filter.
 */
public class LoudnessConfiguration {
    public String input_i;
    public String input_tp;
    public String input_lra;
    public String input_thresh;
    public String output_i;
    public String output_tp;
    public String output_lra;
    public String output_thresh;
    public String normalization_type;
    public String target_offset;

    public LoudnessConfiguration(LoudnessConfiguration configuration) {
        this.input_i = configuration.input_i;
        this.input_tp = configuration.input_tp;
        this.input_lra = configuration.input_lra;
        this.input_thresh = configuration.input_thresh;
        this.output_i = configuration.output_i;
        this.output_tp = configuration.output_tp;
        this.output_lra = configuration.output_lra;
        this.output_thresh = configuration.output_thresh;
        this.normalization_type = configuration.normalization_type;
        this.target_offset = configuration.target_offset;
    }

    public LoudnessConfiguration() {
    }


    public void checkBounds() {
        double d_input_i = Double.parseDouble(input_i);
        double d_input_tp = Double.parseDouble(input_tp);
        double d_input_lra = Double.parseDouble(input_lra);
        double d_target_offset = Double.parseDouble(target_offset);

        // limits set by ffmpeg. See: https://ffmpeg.org/ffmpeg-all.html#loudnorm
        if (d_input_i < -70) {
            d_input_i = -70;
        }
        else if (d_input_i > -5) {
            d_input_i = -5;
        }

        if (d_input_tp < -9) {
            d_input_tp = -9;
        }
        else if (d_input_tp > 0) {
            d_input_tp = 0;
        }

        if (d_input_lra < 1) {
            d_input_lra = 1;
        }
        else if (d_input_lra > 50) {
            d_input_lra = 50;
        }

        if (d_target_offset < -99) {
            d_target_offset = -99;
        }
        else if (d_target_offset > 99) {
            d_target_offset = 99;
        }

        input_i = String.format(Locale.US, "%1$,.2f", d_input_i);
        input_tp = String.format(Locale.US, "%1$,.2f", d_input_tp);
        input_lra = String.format(Locale.US, "%1$,.2f", d_input_lra);
        target_offset = String.format(Locale.US, "%1$,.2f", d_target_offset);
    }

    public String getConfigTarget() {
        checkBounds();
        return ":i=" + input_i +
                ":tp=" + input_tp +
                ":lra=" + input_lra +
                ":offset=" + target_offset;
    }

    public String getConfigMeasured() {
        return ":measured_i=" + input_i +
                ":measured_tp=" + input_tp +
                ":measured_lra=" + input_lra +
                ":measured_thresh=" + input_thresh;
    }

    public void setLUFSValue(double lufsValue) {
        input_i = String.format(Locale.US, "%1$,.2f", lufsValue);
    }
}
