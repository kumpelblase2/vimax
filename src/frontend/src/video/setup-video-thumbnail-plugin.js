import videojs from 'video.js';
import { Html5ThumbnailsPlugin } from "../vendor/videojs-thumbnails";

videojs.registerPlugin('thumbnails', Html5ThumbnailsPlugin);
