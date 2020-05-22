package net.bramp.ffmpeg.builder

import com.google.common.base.Preconditions
import com.google.common.base.Strings
import com.google.common.collect.ImmutableList
import net.bramp.ffmpeg.FFmpegUtils
import java.util.concurrent.TimeUnit

// Sadly this hack is necessary to re-order input args.
// The used library adds the input file just before the output file.
// However, ffmpeg needs the input to be _before_ the -vframes option.
// And since all variables are package private, we can't just override
// the existing implementation but also have to use the same package
// to get access to them.
class CustomFFmpegBuilder : FFmpegBuilder() {

    override fun build(): List<String> {
        val args = ImmutableList.Builder<String>()

        Preconditions.checkArgument(!inputs.isEmpty(), "At least one input must be specified")
        Preconditions.checkArgument(!outputs.isEmpty(), "At least one output must be specified")

        args.add(if (override) "-y" else "-n")
        args.add("-v", this.verbosity.toString())


        if (user_agent != null) {
            args.add("-user_agent", user_agent)
        }

        if (startOffset != null) {
            args.add("-ss", FFmpegUtils.toTimecode(startOffset, TimeUnit.MILLISECONDS))
        }

        if (format != null) {
            args.add("-f", format)
        }

        if (read_at_native_frame_rate) {
            args.add("-re")
        }

        if (progress != null) {
            args.add("-progress", progress.toString())
        }

        for (input in inputs) {
            args.add("-i", input)
        }
        
        args.addAll(extra_args)

        if (pass > 0) {
            args.add("-pass", Integer.toString(pass))

            if (pass_prefix != null) {
                args.add("-passlogfile", pass_directory + pass_prefix)
            }
        }

        if (!Strings.isNullOrEmpty(complexFilter)) {
            args.add("-filter_complex", complexFilter)
        }

        if (!Strings.isNullOrEmpty(videoFilter)) {
            args.add("-vf", videoFilter)
        }

        if (!Strings.isNullOrEmpty(audioFilter)) {
            args.add("-af", audioFilter)
        }

        for (output in this.outputs) {
            args.addAll(output.build(this, pass))
        }

        return args.build()
    }
}
