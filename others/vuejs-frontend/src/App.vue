<template>
    <metainfo></metainfo>
    <div v-if="isStyleLoaded" id="app" class="app">
        <CustomHeader/>
        <main>
            <nav class="navigation-drawer" v-show="isDrawerOpen" :style="{background: `${themeValues.primaryColor}`}">
                <TreeViewNode v-for="(motherCategory, index) in tree" :key="index" :node="motherCategory" :depth="0"/>
            </nav>
            <div class="page-content">
                <router-view v-if="!isFetching" class="page-content__router-view" :key="$route.fullPath"/>
                <Spinner v-else/>
            </div>
        </main>
        <LightBox/>
    </div>
    <Spinner v-else/>
</template>

<script>
    import {mapState} from "pinia";
    import Spinner from "./components/UI/Spinner";
    import CustomHeader from "./components/UI/CustomHeader";
    import LightBox from "./components/UI/LightBox";
    import TreeViewNode from "./components/UI/TreeViewNode";
    import {useLessonStore} from '@/stores/lesson';
    import {useUiStore} from '@/stores/ui';
    import {useTreeStore} from '@/stores/tree';

    export default {
        name: 'App',
        setup() {
            return {
                lessonStore: useLessonStore(),
                uiStore: useUiStore(),
                treeStore: useTreeStore()
            }
        },
        components: {LightBox, CustomHeader, TreeViewNode, Spinner},
        data: () => ({
            isFetching: true,
            isStyleLoaded: false,
        }),
        computed: {
        ...mapState(useUiStore, ['isDrawerOpen','themeValues','isSortedByTag']),
        ...mapState(useTreeStore, ['tree']),
        },
        created() {
            if (this.$router.currentRoute.name === 'Lesson') this.isUserOnLesson = true;
            this.uiStore.fetchStyle({smp : this.$smp}).finally(() => {
                this.isStyleLoaded = true;
            });
            this.uiStore.fetchTags({smp: this.$smp});
            this.treeStore.fetchTree({smp: this.$smp}).then(() => this.isFetching = false);
        },
        watch: {
            $route(to) {
                this.isUserOnLesson = to.name === 'Lesson';
            }
        },
        metaInfo(){
            return {
                // Children can override the title.
                title: 'App',
                titleTemplate: 'Docs | %s',
                // Define meta tags here.
                meta: [
                    {'http-equiv': 'Content-Type', 'content': 'text/html; charset=utf-8'},
                    {'name': 'viewport', 'content': 'width=device-width, initial-scale=1'},
                    {'name': 'description', 'content': 'I have things here on my site.'},
                ],
                /* link: [
                    {  'rel': 'icon', 'size': '192x192', 'href': 'simplicite.svg'}
                ] */
            }
        }
    };
</script>

<style lang="sass">
@import "assets/sass/variables"

*
    box-sizing: border-box
    margin: 0
    outline: none
    scrollbar-color: #999 transparent
    scrollbar-width: thin
    
button
    border: 0
    background-color: transparent
    color: inherit
#app
    height: 100%
.app
    font-family: 'Source Sans Pro', sans-serif
    
    height: 100%
    display: flex
    flex-direction: column
    overflow: hidden

    main
        flex: 1 1 // So the main content extends to the bottom of the page
        display: flex
        flex-direction: row
        width: 100%
        position: relative
        .navigation-drawer
            box-sizing: border-box
            min-width: max-content
            max-height: 100vh - $header-height
            z-index: 1000
            overflow-y: auto
            display: block
            transition: $duration-drawer-collapse ease-in-out
            position: relative

        .page-content
            width: 100%

            &__router-view
                width: 100%
                height: 100%

    .drawer-enter-active
        animation: drawerIn $duration-drawer-collapse

    .drawer-leave-active
        animation: drawerOut $duration-drawer-collapse

@keyframes drawerIn
    from
        width: 0
    to
        width: $drawer-width

@keyframes drawerOut
    from
        width: $drawer-width
    to
        width: 0

</style>
