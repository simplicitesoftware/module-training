<template>
    <div class="toc-wrapper" :style="{ background: uiStore.themeValues.neutralColor }">
        <div class="toc" ref="toc" :style="{ background: uiStore.themeValues.neutralColor }">
            <div class="toc__header">
                <h3 class="toc__title">{{ currentTitle }}</h3>
            </div>
            <Spinner v-if="!headings.length"/>
            <ul v-else class="toc__list">
                <li v-for="(heading) in headings" 
                    :key="heading.id"
                    :ref="heading.id"
                    :class="[
                        'toc__item', 
                        `toc__item--h${heading.level}`,
                        { 'toc__item--active': activeId === heading.id }
                    ]">
                    <a :href="`#${heading.id}`" 
                    @click.prevent="handleClick(heading.id)"
                    class="toc__link">
                        {{ heading.text }}
                    </a>
                </li>
            </ul>
        </div>
    </div>
</template>

<script>
import { mapState } from "pinia";
import { useUiStore } from '@/stores/ui';
import Spinner from "./Spinner.vue";

export default {
    name: 'TableOfContents',
    components: { Spinner },
    setup() {
        return {
            uiStore: useUiStore(),
        }
    },
    computed: {
        ...mapState(useUiStore, ['lang']),
    },
    data() {
        return {
            headings: [],
            currentTitle: 'Table of Contents',
            spinner: true,
            activeId: null,
            clickedId: null,
            observer: null,
            visibleHeadings: new Set(),
            isUserClicked: false
        }
    },
    watch: {
        lang: {
            handler() {
                setTimeout(() => {
                    this.waitForContent()
                    
                    setTimeout(() => {
                        this.waitForContent()
                    }, 1500)
                }, 2000)
            }
        },
        '$route': {
            handler() {
                // Reset and reinitialize when route changes
                if (this.observer) {
                    this.observer.disconnect()
                }
                this.visibleHeadings.clear()
                this.activeId = null
                
                // Wait for new content to be rendered
                this.$nextTick(() => {
                    this.waitForContent()
                    this.setupIntersectionObserver()
                })
            },
            immediate: true
        },
        activeId(newId) {
            if (newId) {
                this.$nextTick(() => {
                    this.scrollActiveHeadingIntoView(newId)
                })
            }
        }
    },
    mounted() {
        this.waitForContent()
        this.setupIntersectionObserver()
        
        // Update TOC when route changes
        this.$watch(
            () => this.$route.path,
            async () => {
                await this.$nextTick()
                this.waitForContent()
            }
        )
    },
    beforeUnmount() {
        if (this.observer) {
            this.observer.disconnect()
        }
    },
    methods: {
        updateTitle() {
            this.currentTitle = this.lang === 'FRA' ? 'Table des matières' : 'Table of Contents'
        },
        waitForContent(attempts = 0, maxAttempts = 20) {
            if (attempts >= maxAttempts) {
                console.warn('Max attempts reached waiting for content')
                return
            }

            const content = document.querySelector('.lesson-html-content')
            if (content) {
                this.generateTOC()
                this.setupIntersectionObserver() // Reinitialize observer after TOC generation
            } else {
                setTimeout(() => this.waitForContent(attempts + 1), 250)
            }
        },
        generateTOC() {
            // Look for headings in the lesson content
            const markdownContent = document.querySelector('.lesson-html-content')
            if (!markdownContent) {
                console.warn('No content container found')
                return
            }

            // Get all h1-h3 elements from the content
            const headings = Array.from(markdownContent.querySelectorAll('h1, h2, h3, h4'))
            if (headings.length === 0) {
                console.warn('No headings found in content')
                return
            }

            this.headings = headings.map(heading => ({
                id: heading.id || this.generateId(heading),
                text: heading.childNodes[0].textContent.trim(),
                level: parseInt(heading.tagName.charAt(1)),
            }))
            
            // Update title when TOC is generated
            this.updateTitle()
            
            // Add IDs to headings that don't have them
            headings.forEach(heading => {
                if (!heading.id) {
                    heading.id = this.generateId(heading)
                }
            })

            this.spinner = false;

        },
        generateId(heading) {
            return heading.textContent
                .toLowerCase()
                .replace(/\s+/g, '-')
                .replace(/[^\w-]/g, '')
                
        },
        scrollToHeading(id) {
            const element = document.getElementById(id)
            if (element) {
                element.scrollIntoView({ behavior: 'smooth' })
            }
        },
        setupIntersectionObserver() {
            const options = {
                root: null,
                rootMargin: '-20% 0px -70% 0px',
                threshold: 0
            }

            this.observer = new IntersectionObserver((entries) => {
                // Don't update active section if user just clicked
                if (this.isUserClicked) return
                
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        this.visibleHeadings.add(entry.target.id)
                    } else {
                        this.visibleHeadings.delete(entry.target.id)
                    }
                    
                    const visibleHeadingElements = Array.from(document.querySelectorAll('.lesson-html-content h1, .lesson-html-content h2, .lesson-html-content h3'))
                        .filter(heading => this.visibleHeadings.has(heading.id))
                    
                    if (visibleHeadingElements.length > 0) {
                        this.activeId = visibleHeadingElements[0].id
                    }
                })
            }, options)

            setTimeout(() => {
                const headings = document.querySelectorAll('.lesson-html-content h1, .lesson-html-content h2, .lesson-html-content h3')
                headings.forEach(heading => this.observer.observe(heading))
            }, 1000)
        },
        scrollActiveHeadingIntoView(headingId) {
            const tocElement = this.$refs.toc
            const activeElement = this.$refs[headingId]?.[0]
            
            if (!tocElement || !activeElement) return
            
            const tocRect = tocElement.getBoundingClientRect()
            const activeRect = activeElement.getBoundingClientRect()
            
            // Calculate if element is outside visible area
            const isAbove = activeRect.top < tocRect.top + 80 // Add offset for title
            const isBelow = activeRect.bottom > tocRect.bottom
            
            if (isAbove || isBelow) {
                const scrollOffset = isAbove ? 
                    activeElement.offsetTop - 80 : // Scroll to put element at top (with offset)
                    activeElement.offsetTop - tocRect.height + activeRect.height + 80 // Scroll to put element at bottom
                
                tocElement.scrollTo({
                    top: scrollOffset,
                    behavior: 'smooth'
                })
            }
        },
        handleClick(id) {
            this.isUserClicked = true
            this.activeId = id
            this.scrollToHeading(id)
            
            // Reset isUserClicked after scroll animation completes
            setTimeout(() => {
                this.isUserClicked = false
            }, 1000)
        }
    }
}
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

.toc-wrapper
    z-index: 5

.toc
    position: relative
    height: 100%
    overflow-y: auto
    background: #fff
    border-radius: map-get($radius, regular)
    @include box-shadow
    backdrop-filter: blur(10px)
    color: var(--text-color, #333)
    scroll-behavior: smooth

    &__header
        position: sticky
        top: 0
        background: inherit
        padding: 1rem 1rem 0.5rem
        margin-bottom: 0.5rem
        z-index: 10
        border-bottom: 1px solid rgba(0, 0, 0, 0.1)
        backdrop-filter: blur(10px)

    &__title
        margin: 0
        font-size: 1.2rem
        font-weight: 600

    &__list
        list-style: none
        padding: 0 1rem
        margin: 0

    &__item
        margin: 0.5rem 0
        transition: all 0.2s ease
        
        &--h1
            margin-left: 0
        &--h2
            margin-left: 1rem
        &--h3
            margin-left: 2rem
        &--h4
            margin-left: 3rem
            
        &--active
            > .toc__link
                color: var(--color-accent, #228be6)
                font-weight: 600
                transform: translateX(4px)

    &__link
        color: inherit
        text-decoration: none
        display: block
        transition: all 0.2s ease
        
        &:hover
            color: var(--color-accent, #228be6)
            transform: translateX(4px)

    // Spinner positioning
    .spinner
        position: absolute
        top: 50%
        left: 50%
        transform: translate(-50%, -50%)
        height: 50px
        width: 50px
</style> 