<template>
    <div class="root-container">
        <metainfo></metainfo>
        <div v-if="isStyleLoaded" id="app" class="app">
            <CustomHeader/>
            <main>
                <nav class="navigation-drawer" v-show="isDrawerOpen" :style="{background: `linear-gradient(${themeValues.primaryColor} 65%, ${themeValues.secondaryColor})`}">
                    <MenuSearchBar v-model="searchQuery" class="search-bar" />
                    <TreeViewNode v-for="(motherCategory, index) in filteredTree" :key="index" :node="motherCategory" :depth="0" :searchQuery="searchQuery"/>
                </nav>
                <div class="page-content">
                    <router-view v-if="!isFetching" class="page-content__router-view" :key="$route.fullPath"/>
                    <Spinner v-else/>
                    <TableOfContents class="page-content_toc" v-if="!isFetching"/>
                    <Spinner v-else/>
                </div>
            </main>
            <LightBox/>
        </div>
        <Spinner v-else/>
    </div>
</template>

<script>
    import {mapState} from "pinia";
    import Spinner from "./components/UI/Spinner";
    import CustomHeader from "./components/UI/CustomHeader";
    import LightBox from "./components/UI/LightBox";
    import TreeViewNode from "./components/UI/TreeViewNode";
    import MenuSearchBar from "./components/UI/MenuSearchBar.vue";
    import {useLessonStore} from '@/stores/lesson';
    import {useUiStore} from '@/stores/ui';
    import {useTreeStore} from '@/stores/tree';
    import TableOfContents from "./components/UI/TableOfContents.vue";

    export default {
        name: 'App',
        setup() {
            return {
                lessonStore: useLessonStore(),
                uiStore: useUiStore(),
                treeStore: useTreeStore()
            }
        },
        components: {LightBox, CustomHeader, TreeViewNode, Spinner, MenuSearchBar, TableOfContents},
        data: () => ({
            isFetching: true,
            isStyleLoaded: false,
            searchQuery: "",
            originalTree: [], // Store the original tree data here
        }),
        computed: {
            ...mapState(useUiStore, ['isDrawerOpen','themeValues','isSortedByTag']),
            ...mapState(useTreeStore, ['tree']),

            filteredTree() {
                if (this.searchQuery.trim()) {
                    return this.tree
                        .map((node) => this.filterTree(node, this.searchQuery.toLowerCase()))
                        .filter((node) => node !== null);
                }
                return this.originalTree; // Reset to original tree when search is cleared
            },
        },
        created() {
            if (this.$router.currentRoute.name === 'Lesson') this.isUserOnLesson = true;
            this.uiStore.fetchStyle({smp : this.$smp}).finally(() => {
                this.isStyleLoaded = true;
                this.uiStore.setContentLoaded();
            });
            this.uiStore.fetchTags({smp: this.$smp});
            this.treeStore.fetchTree({smp: this.$smp}).then(() =>  {
                this.isFetching = false;
                this.originalTree = [...this.tree]; // Store the original tree once it's fetched
            });
        },
        watch: {
            $route(to) {
                this.isUserOnLesson = to.name === 'Lesson';
                this.handlePageContentChange();
            },
            tree(newTree) {
                // Store the initial tree structure
                if (this.originalTree.length === 0 && newTree.length > 0) {
                    this.originalTree = [...newTree]; // Clone the original tree to reset later
                }
            },
            searchQuery(newQuery) {
                if (newQuery === "") {
                    // If the search is cleared, reset the tree to its original state
                    this.$nextTick(() => {
                        this.treeStore.SET_TREE(this.originalTree); // Use treeStore to reset or modify tree if needed
                    });
                }
            },
        },
        methods: {
            handlePageContentChange() {
                this.uiStore.setContentLoaded();
            },
            filterTree(node, query) {
                if (!node || !node.title) return null; // Ensure node and title exist

                const matches = node.title.toLowerCase().includes(query) && !node.is_category;

                if (node.items && node.items.length > 0) {
                    const filteredChildren = node.items
                        .map(child => this.filterTree(child, query))
                        .filter(child => child !== null);
                    if (matches || filteredChildren.length > 0) {
                        this.treeStore.OPEN_NODE(node.path);
                        return { ...node, items: filteredChildren }; // Keep parent if child matches
                    }
                }

                return matches ? { ...node, items: [] } : null; // Keep matching nodes
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
    font-family: "Source Sans Pro", sans-serif
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
            width: 20vw
            z-index: 1000
            overflow-y: auto
            display: block
            transition: $duration-drawer-collapse ease-in-out
            position: relative

            .search-bar 
                width: 100% 
                padding: 8px

        .page-content 
            display: flex
            width: 100%

            &__router-view 
                width: 80%
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
                
.root-container
    height: 100%
</style>
