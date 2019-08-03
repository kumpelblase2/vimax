// Based on https://github.com/ajbogh/video-js-html5Thumbnails, though heavily modified actually work


import videojs from 'video.js';

function buildThumbnailContainers(settings, player) {
    const thumbHeight = settings.thumbnailHeight;
    const thumbWidth = settings.thumbnailWidth;

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

    return {
        div: div,
        video: video,
        progressControl: progressControl
    };
}

function showThumb(div) {
    div.style.opacity = '1';
    div.style.display = 'block';
}

function getThumbnailXOffset(seekRect, progress, settings) {
    const offset = (seekRect.width * progress) - (settings.thumbnailWidth / 2);
    return Math.min(Math.max(offset, 0), seekRect.width - settings.thumbnailWidth);
}

export function Html5ThumbnailsPlugin(options) {
    const settings = Object.assign({ thumbnailWidth: 250, thumbnailHeight: 140, play: false }, options);
    const player = this;

    const thumbnailElems = buildThumbnailContainers(settings, player);
    const div = thumbnailElems.div;
    const video = thumbnailElems.video;
    const progressControl = thumbnailElems.progressControl;

    let hideInterval;

    const updateThumbnail = (event) => {
        clearInterval(hideInterval);
        const progress = videojs.dom.getPointerPosition(progressControl.seekBar.el(), event).x;
        const seekRect = videojs.dom.getBoundingClientRect(progressControl.seekBar.el());
        showThumb(div);

        div.style.left = getThumbnailXOffset(seekRect, progress, settings) + "px";
        video.currentTime = progress * player.duration();
    };

    progressControl.el().addEventListener('mouseover', (event) => {
        updateThumbnail(event);

        if(settings.play) {
            video.play().catch(ex => null);
        }
    }, false);

    progressControl.el().addEventListener('mousemove', updateThumbnail, false);
    progressControl.el().addEventListener('mouseout', () => {
        video.pause(); //make sure the video doesn't continue downloading on mouse out.
        hideInterval = setInterval(function() {
            if(div.style.opacity <= 0) {
                div.style.display = "none";
                clearInterval(hideInterval);
            }
            div.style.opacity = +(div.style.opacity) - 0.1;
        }, 10);
    }, false);
}
