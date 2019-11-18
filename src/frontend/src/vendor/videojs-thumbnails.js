// Based on https://github.com/ajbogh/video-js-html5Thumbnails, though heavily modified to actually work
import videojs from 'video.js';

const Plugin = videojs.getPlugin('plugin');

function getThumbnailXOffset(seekRect, progress, settings) {
    const offset = (seekRect.width * progress) - (settings.thumbnailWidth / 2);
    return Math.min(Math.max(offset, 0), seekRect.width - settings.thumbnailWidth);
}

export class Html5ThumbnailsPlugin extends Plugin {
    constructor(player, options) {
        super(player, options);
        this.player = player;

        this.settings = Object.assign({ thumbnailWidth: 250, thumbnailHeight: 140, play: false }, options);

        this.buildThumbnailContainers(player);

        this.videoProgressControl.el().addEventListener('mouseover', (event) => {
            this.updateThumbnail(event);

            if(this.settings.play) {
                this.thumbnailVideo.play().catch(ex => null);
            }
        }, false);

        this.videoProgressControl.el().addEventListener('mousemove', e => this.updateThumbnail(e), false);
        this.videoProgressControl.el().addEventListener('mouseout', () => {
            this.thumbnailVideo.pause(); //make sure the video doesn't continue downloading on mouse out.
            this.hideThumb();
        }, false);
    }

    src(newUrl) {
        this.thumbnailVideo.src = newUrl;
    }

    updateThumbnail(event) {
        clearInterval(this.hideInterval);
        this.showThumb();

        const progress = videojs.dom.getPointerPosition(this.videoProgressControl.seekBar.el(), event).x;
        const seekRect = videojs.dom.getBoundingClientRect(this.videoProgressControl.seekBar.el());

        this.thumbnailContainer.style.left = getThumbnailXOffset(seekRect, progress, this.settings) + "px";
        let displayTime = progress * this.player.duration();
        this.thumbnailVideo.currentTime = isNaN(displayTime) ? 0 : displayTime;
    }

    showThumb() {
        this.thumbnailContainer.style.opacity = '1';
        this.thumbnailContainer.style.display = 'block';
    }

    hideThumb() {
        this.hideInterval = setInterval(() => {
            if(this.thumbnailContainer.style.opacity <= 0) {
                this.thumbnailContainer.style.display = "none";
                clearInterval(this.hideInterval);
            }
            this.thumbnailContainer.style.opacity = +(this.thumbnailContainer.style.opacity) - 0.1;
        }, 10);
    }

    buildThumbnailContainers(player) {
        const thumbHeight = this.settings.thumbnailHeight;
        const thumbWidth = this.settings.thumbnailWidth;

        const mainPlayerVideo = (player.tag || player.tech().el());
        const video = mainPlayerVideo.cloneNode(true);
        video.className = "";
        video.removeAttribute("data-setup");
        video.muted = true;
        video.id = "vjs-thumbnail-video";
        video.className = 'vjs-thumbnail-video';
        video.src = player.src();

        // create the thumbnail
        const div = document.createElement('div');
        div.className = 'vjs-thumbnail-holder';

        //calculate the thumbnail width and height
        div.height = thumbHeight;
        div.width = thumbWidth;

        //set the thumbnail container width and height
        div.style.width = div.width;
        div.style.height = div.height;

        //set the thumbnail container width and height
        video.height = div.height;
        video.width = div.width;

        //hide the thumbnail by default
        div.style.opacity = 0;

        //append the video thumbnail to its container
        div.appendChild(video);

        //adjust the position
        div.style.top = `-${thumbHeight}px`;
        div.style.position = "absolute";

        // add the thumbnail to the player
        const progressControl = player.controlBar.progressControl;
        progressControl.el().appendChild(div);

        this.thumbnailContainer = div;
        this.thumbnailVideo = video;
        this.videoProgressControl = progressControl;
    }
}
