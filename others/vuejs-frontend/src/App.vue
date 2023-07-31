<template>
    <div id="app" class="app">
        <CustomHeader v-if="isStyleLoaded"/>
        <main>
            <nav class="navigation-drawer" v-show="isDrawerOpen" :style="{background: `linear-gradient(${themeValues.primaryColor} 65%, ${themeValues.secondaryColor})`}">
                <TreeViewNode v-for="(motherCategory, index) in tree" :key="index" :node="motherCategory" :depth="0"/>
            </nav>
            <div class="page-content">
                <router-view v-if="!isFetching" class="page-content__router-view" :key="$route.fullPath"/>
                <Spinner v-else/>
            </div>
        </main>
        <LightBox/>
    </div>
</template>

<script>
    import {mapGetters, mapState} from "vuex";
    import Spinner from "./components/UI/Spinner";
    import CustomHeader from "./components/UI/CustomHeader";
    import LightBox from "./components/UI/LightBox";
    import TreeViewNode from "./components/UI/TreeViewNode";

    export default {
        name: 'App',
        components: {LightBox, CustomHeader, TreeViewNode, Spinner},
        data: () => ({
            isFetching: true,
            isStyleLoaded: false,
        }),
        computed: {
        ...mapState({
            tree: state => state.tree.tree,
            isDrawerOpen: state => state.ui.isDrawerOpen,
            themeValues: state => state.ui.themeValues,
        }),
        ...mapGetters({
            isSortedByTag: "ui/isSortedByTag",
        })
        },
        created() {
            if (this.$router.currentRoute.name === 'Lesson') this.isUserOnLesson = true;
            this.$store.dispatch('ui/fetchStyle', {smp : this.$smp}).finally(() => {
                this.isStyleLoaded = true;
            });
            this.$store.dispatch('ui/fetchTags', {smp: this.$smp});
            this.$store.dispatch('tree/fetchTree', {smp: this.$smp}).then(() => this.isFetching = false);
        },
        watch: {
            $route(to) {
                this.isUserOnLesson = to.name === 'Lesson';
            }
        },
        metaInfo: {
            // Children can override the title.
            title: 'App',
            titleTemplate: 'Docs | %s',
            // Define meta tags here.
            meta: [
                {'http-equiv': 'Content-Type', 'content': 'text/html; charset=utf-8'},
                {'name': 'viewport', 'content': 'width=device-width, initial-scale=1'},
                {'name': 'description', 'content': 'I have things here on my site.'}
            ]
        }
    };
</script>

<style lang="sass">
@import "assets/sass/variables"

*
    box-sizing: border-box
    margin: 0
    outline: none

button
    border: 0
    background-color: transparent
    color: inherit

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
            overflow-y: scroll
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
