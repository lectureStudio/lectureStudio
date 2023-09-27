package org.lecturestudio.media.audio;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.lecturestudio.core.io.RandomAccessAudioStream;
import org.lecturestudio.core.recording.file.RecordingUtils;
import org.lecturestudio.core.util.OsInfo;
import org.lecturestudio.core.util.ProgressCallback;

public class FFmpegLoudnessNormalization {

    private LoudnessConfiguration loudnessConfiguration;

    /**
     * Analyzes the audio clip with ffmpeg using the loudnorm filter and returns the loudness information.
     * The loudnorm filter analyzes the perceived volume.
     * This step is not needed when changing the perceived loudness of a track, but it increases the quality of the
     * output audio clip of the second step significantly, and therefore it is required in the context of this class.
     *
     * @param input    The audio clip to be analyzed
     * @param callback The progress callback
     * @return The loudness information of the provided track
     * @throws IOException
     */
    public LoudnessConfiguration retrieveInformation(RandomAccessAudioStream input, ProgressCallback callback) throws IOException {
        File inputFile = Files.createTempFile("lect-editor-", ".wav").toFile();
        inputFile.deleteOnExit();

        RecordingUtils.exportAudio(input, inputFile, (progress -> {
        }));

        loudnessConfiguration = getStatsFromFile(inputFile, callback);
        callback.onProgress(1);

        return loudnessConfiguration;
    }

    /**
     * Normalizes the provided audio clip using the ffmpeg loudnorm filter.
     * It adjusts the perceived volume of the provided audio clip to match
     * the perceived volume of the previously analyzed clip.
     *
     * @param input    The audio clip to be normalized
     * @param callback The progress callback
     * @return The normalized audio clip as a file
     * @throws IOException
     */
    public File normalize(RandomAccessAudioStream input, ProgressCallback callback) throws IOException {

        File inputFile = Files.createTempFile("lect-editor-", ".wav").toFile();
        inputFile.deleteOnExit();

        RecordingUtils.exportAudio(input, inputFile, (progress -> {
        }));

        int totalSteps = 2;
        LoudnessConfiguration measuredConfiguration = getStatsFromFile(inputFile, (progress) -> callback.onProgress(progress / totalSteps));

        callback.onProgress(0.5f);
        File normalized = normalize(input, measuredConfiguration, loudnessConfiguration, inputFile, (progress) -> callback.onProgress((progress + 1) / totalSteps));
        callback.onProgress(1);
        return normalized;
    }

    public File normalize(RandomAccessAudioStream input, LoudnessConfiguration measuredConfiguration,
                          LoudnessConfiguration targetConfiguration, ProgressCallback callback) throws IOException {

        File inputFile = Files.createTempFile("lect-editor-", ".wav").toFile();
        inputFile.deleteOnExit();

        RecordingUtils.exportAudio(input, inputFile, (progress -> {
        }));

        return normalize(input, measuredConfiguration, targetConfiguration, inputFile, callback);
    }

    private File normalize(RandomAccessAudioStream input, LoudnessConfiguration measuredConfiguration,
                           LoudnessConfiguration targetConfiguration, File inputFile, ProgressCallback callback) throws IOException {
        File outputFile = Files.createTempFile("lect-editor-", ".wav").toFile();
        outputFile.deleteOnExit();

        List<String> normalizeCommands = getNormalizeCommands(input, measuredConfiguration, targetConfiguration);


        ProcessBuilder ffmpegProcessBuilder = getFFmpegProcessBuilder(inputFile.getAbsolutePath(),
                FilenameUtils.getExtension(inputFile.getName()), outputFile.getAbsolutePath(),
                null, null, normalizeCommands);

        Process process = ffmpegProcessBuilder.start();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        FFmpegProgressReader progressReader = new FFmpegProgressReader();
        String line;
        StringBuilder builder = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
            progressReader.checkProgress(line, callback);
        }

        int returnCode;
        try {
            returnCode = process.waitFor();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (returnCode == 0) {
            return outputFile;
        }
        else {
            throw new RuntimeException(builder.toString());
        }
    }

    /**
     * Generates the necessary commands for loudness normalization.
     * -filter:a loudnorm=linear=true loud_norm filter
     * dual_mono treats mono audio different from stereo content (decreased volume by 3 LUFS)
     * -ar sets output sample rate
     * -ac sets the output channel amount
     *
     * @param input The input audio stream, needed for additional information
     * @param measuredConfiguration The measured configuration, needed for analyzed information
     * @param targetConfiguration   The target configuration, needed for output information
     * @return A list of commands used for ffmpeg loudnorm normalization
     */
    private static List<String> getNormalizeCommands(RandomAccessAudioStream input, LoudnessConfiguration measuredConfiguration, LoudnessConfiguration targetConfiguration) {
        List<String> normalizeCommands = new ArrayList<>();
        normalizeCommands.add("-filter:a");
        normalizeCommands.add("loudnorm=linear=true" + targetConfiguration.getConfigTarget() + measuredConfiguration.getConfigMeasured() + ":dual_mono=true");
        normalizeCommands.add("-ar");
        normalizeCommands.add(input.getAudioFormat().getSampleRate() + "");
        normalizeCommands.add("-ac");
        normalizeCommands.add(input.getAudioFormat().getChannels() + "");
        return normalizeCommands;
    }

    /**
     * Analyzes the Audiofile with ffmpeg loudnorm and returns the settings.
     * Analyzing time is about 1/60 of the total duration of the audio clip.
     *
     * @param inputFile The audio file to analyze
     * @return The LoudnessConfiguration of the audio file
     * @throws IOException
     */
    public LoudnessConfiguration getStatsFromFile(File inputFile, ProgressCallback callback) throws IOException {
        List<String> normalizeCommands = new ArrayList<>();
        normalizeCommands.add("-filter:a");
        normalizeCommands.add("loudnorm=print_format=json");

        ProcessBuilder ffmpegProcessBuilder = getFFmpegProcessBuilder(inputFile.getAbsolutePath(), "null",
                "NULL", null, null, normalizeCommands);
        Process process = ffmpegProcessBuilder.start();


        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));


        StringBuilder builder = new StringBuilder();
        FFmpegProgressReader progressReader = new FFmpegProgressReader();
        boolean startParsing = false;
        String line;

        // The input stream looks like the following:
        // lines with extra information
        // [Parsed_loudnorm_
        // json of LoudnessConfiguration
        while ((line = reader.readLine()) != null) {
            progressReader.checkProgress(line, callback);
            if (startParsing) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            else if (line.contains("[Parsed_loudnorm_")) {
                startParsing = true;
            }
        }

        int returnCode;
        try {
            returnCode = process.waitFor();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        ObjectMapper mapper = new ObjectMapper();
        String result = builder.toString();

        if (returnCode == 0) {
            return mapper.readValue(result, LoudnessConfiguration.class);
        }
        else {
            throw new RuntimeException(process.info().toString());
        }
    }

    /**
     * Creates an FFmpeg process builder, that can apply filters to audio files.
     *
     * @param inputPath           The input for ffmpeg
     * @param outputFileFormat    The output file format
     * @param outputPath          The output for ffmpeg
     * @param extraGlobalCommands Extra commands regarding ffmpeg
     * @param extraInputCommands  Extra commands regarding the input
     * @param extraOutputCommands Extra commands regarding the output
     * @return A process ffmpeg process builder
     */
    private ProcessBuilder getFFmpegProcessBuilder(String inputPath,
                                                   String outputFileFormat,
                                                   String outputPath,
                                                   List<String> extraGlobalCommands,
                                                   List<String> extraInputCommands,
                                                   List<String> extraOutputCommands) {
        // Load native FFmpeg.
        String platformName = OsInfo.getPlatformName();
        String libraryPath = System.getProperty("java.library.path", "lib/native/" + platformName);
        String ffmpegExec = libraryPath + "/ffmpeg";

        List<String> commands = new ArrayList<>();
        commands.add(ffmpegExec);

        if (extraGlobalCommands != null) {
            commands.addAll(extraGlobalCommands);
        }

        if (extraInputCommands != null) {
            commands.addAll(extraInputCommands);
        }

        commands.add("-i");
        commands.add(inputPath);

        // Use the full horsepower.
        commands.add("-threads");
        commands.add("auto");

        if (extraOutputCommands != null) {
            commands.addAll(extraOutputCommands);
        }

        commands.add("-f");
        commands.add(outputFileFormat);
        commands.add(outputPath);

        commands.add("-y");

        commands.add("-hide_banner");
        commands.add("-loglevel");
        commands.add("info");

        ProcessBuilder procBuilder = new ProcessBuilder(commands.toArray(new String[0]));
        procBuilder.redirectErrorStream(true);
        return procBuilder;
    }

    public LoudnessConfiguration getLoudnessConfiguration() {
        return loudnessConfiguration;
    }

    public void setLoudnessConfiguration(LoudnessConfiguration loudnessConfiguration) {
        this.loudnessConfiguration = loudnessConfiguration;
    }

    /**
     * Reads the progress of the ffmpeg command
     */
    public static class FFmpegProgressReader {
        private int totalDurationInSeconds = -1;


        /**
         * Checks the progress of the ffmpeg command and updates the callback
         *
         * @param line     The line to be checked
         * @param callback The update callback
         */
        public void checkProgress(String line, ProgressCallback callback) {
            if (line.startsWith("size=") && totalDurationInSeconds != -1) {
                // The progress of the precessing. Will always be displayed second
                int startPosition = line.indexOf("time=") + "time=".length();
                String currentProgress = line.substring(startPosition, startPosition + "00:00:00".length());
                int progressInSeconds = convertTimestampToSeconds(currentProgress);
                callback.onProgress((float) progressInSeconds / totalDurationInSeconds);
            }
            else if (line.contains("Duration:")) {
                // The total duration of the audio clip. Will always be displayed first
                int startPosition = line.indexOf("Duration: ") + "Duration: ".length();
                String totalDuration = line.substring(startPosition, startPosition + "00:00:00".length());
                totalDurationInSeconds = convertTimestampToSeconds(totalDuration);

            }
        }

        /**
         * Converts timestamps of the template "00:00:00" into seconds
         *
         * @param currentProgress The timestamp to be converted into seconds
         * @return the converted seconds
         */
        private static int convertTimestampToSeconds(String currentProgress) {
            String[] times = currentProgress.split(":");
            return Integer.parseInt(times[0]) * 60 * 60 + Integer.parseInt(times[1]) * 60 + Integer.parseInt(times[2]);
        }
    }
}
