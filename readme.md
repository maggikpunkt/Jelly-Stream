# JellyStreamer

The goal of this project is to have an easy way to convert MKV files to streamable MP4 files.
Compatibility with simpler clients should be increased while preserving the full quality[^1] for capable clients.

## Why?

MP4 is the most compatible container format and Jellyfin can even stream it without remuxing.
Although Jellyfin usually does a good job converting itself there is the occasional weird file or flaky client that does
not work. Even if the video is fully compatible most of the time it is not optimized for streaming.
Remuxing with `-movflag +faststart` will make it instantly streamable and is a relatively cheap one time operation.
The primary target is Jellyfin but other media servers should work as well.

## How?

FFProbe is used to analyze the files and FFMpeg is used for conversion and transcoding. Subtitles and Audio streams are
transcoded into compatible formats and/or extracted into external files that Jellyfin can handle. For more details see
the help text in the usage section.

## Requirements

- Java 21 Runtime
- FFMpeg and FFProbe

## Shortcomings

- Attachments are currently not supported. This means files with embedded fonts used for subtitles are currently not
  supported.

## Usage

```
Usage: jelly-stream [<options>] <input>

  This script converts .mkv files into .mp4 files optimized for streaming. Incompatible streams are transcoded and/or extracted to files compatible with jellyfin external file naming.

Options:
  -o, --output=<path>                                                    The output directory. Defaults to the directory of the processed file
  -m, --move=<path>                                                      Move processed files to this directory. If not set files will not be moved
  -r, --recursive                                                        Search input directory recursively (default: false)
  -b, --breakOnError, --skipOnError                                      Stops on the first file that could not be processed (default: false)
  --extractStereo                                                        Extracts stereo audio. By default only surround sound audio is extracted (default: false)
  --kBitPerChannel=<int>                                                 The Bitrate per audio channel in kBit/s (default: 64)
  --loglevel=(quiet|panic|fatal|error|warning|info|verbose|debug|trace)  Sets the FFmpeg loglevel. Refer to the FFmpeg documentation (default: warning)
  --stats, --nostats                                                     Sets the FFmpeg 'stats' or 'nostats' flag (default: stats)
  --ffmpeg=<text>                                                        Path to the FFmpeg executable (default: ffmpeg)
  --ffprobe=<text>                                                       Path to the FFprobe executable (default: ffprobe)
  -h, --help                                                             Show this message and exit

Arguments:
  <input>  The input file or directory

Container: The "-movflag +faststart" flag is used to enable instant streaming.

Video: Video is always copied and never transcoded.

Audio: mp4-compatible audio is always copied and never transcoded. Incompatible audio is transcoded to AAC-LC. Incompatible surround sound audio is additionally extracted to external files. This preserves
the original audio while increasing compatibility for the price of having to store an additional audio track.

Subtitles: Subtitles are transcoded to mov_text if possible. More complex subtitles (currently all except subrip) are additionally extracted to external files. hdmv_pgs_subtitle and dvb_subtitle are placed in a
.mks containers because I could not figure out which file extension jellyfin needs for them.

Others: All other stream types are currently not supported and will lead to an error instead of being thrown out silently.
```

[^1]: Make sure to pass the `--extractStereo` Flag if you really want to keep all streams in their original form. By
default, non mp4-compatible stereo audio is only transcoded but not extracted.