<template>
    <VideoCard :video-id="item.id" :skeleton-height="itemHeight" />
</template>

<script>
    import VideoCard from "@/components/video/VideoCard";
    export default {
        name: "VideoCardWrapper",
        props: ['item', 'itemHeight'],
        components: { VideoCard }
    }
</script>

<style scoped>

</style>
