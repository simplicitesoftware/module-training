<template>
	<div class="lesson" :class="lessonViz" :style="{background: bg_color}">
		<div class="grid">
			<div class="grid-item lesson-block" :style="{background: bg_color}">
				<Spinner v-if="spinner" />
				<div v-else-if="lesson.html" class="lesson-wrapper">
					<ul class="breadcrumb">
						<li class="breadcrumb__item" v-for="(item, index) in breadCrumbItems" :key="index">
							<span>{{ item.title }}</span>
							<span class="breadcrumb__divider" v-if="index !== breadCrumbItems.length - 1">></span>
						</li>
					</ul>
					<ul v-if="lessonTags.length > 0" class="tag">
						<li class="tag__list" v-for="(tag, index) in lessonTags" :key="index">
							<span class="tag__item">{{ tag }}</span>
							<span class="breadcrumb__divider" v-if="index !== breadCrumbItems.length - 1"></span>
						</li>
					</ul>
					<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
					<EmptyContent v-else />
				</div>
			</div>
			<TableOfContents class="grid-item toc-block" v-if="lesson.viz == 'LINEAR'"/>
			<div v-if="lesson.viz !== 'LINEAR'" class="grid-item slider-block" :style="{background: bg_color}">
				<Slider v-if="lessonImages.length" :slides="lessonImages" ref="slider" />
				<EmptyContent v-else />
			</div>
			<div v-if="lesson.viz !== 'LINEAR'" class="grid-item video-block" :style="{background: bg_color}">
				<div v-if="lesson" class="video-wrapper">
					<video v-if="videoUrl" class="video-player" controls muted poster="../../../public/media.svg" :src="videoUrl"
						preload="none">
						Sorry, your browser doesn't support embedded videos.
					</video>
					<EmptyContent v-else />
				</div>
				<Spinner v-else />
			</div>
		</div>
	</div>
</template>

<script>
/* eslint-disable no-console,no-unused-vars,no-undef */

import Spinner from "../UI/Spinner";
import EmptyContent from "../UI/EmptyContent";
import Slider from "../UI/Slider";
import { mapState } from "pinia";
import { useLessonStore } from "../../stores/lesson";
import { useTreeStore } from "../../stores/tree";
import { useUiStore } from "../../stores/ui";

import hljs from "highlight.js/lib/core";
import javascript from 'highlight.js/lib/languages/javascript';
import java from 'highlight.js/lib/languages/java';
import xml from 'highlight.js/lib/languages/xml';
import markdown from 'highlight.js/lib/languages/markdown';
import json from 'highlight.js/lib/languages/json';
import plaintext from 'highlight.js/lib/languages/plaintext';
import less from 'highlight.js/lib/languages/less';
import bash from 'highlight.js/lib/languages/bash';
import awk from 'highlight.js/lib/languages/awk';
import css from 'highlight.js/lib/languages/css';
import dockerfile from 'highlight.js/lib/languages/dockerfile';
import yaml from 'highlight.js/lib/languages/yaml';
import ldif from 'highlight.js/lib/languages/ldif';
import nginx from 'highlight.js/lib/languages/nginx';
import apache from 'highlight.js/lib/languages/apache';
import sql from 'highlight.js/lib/languages/sql';
import mermaid from "mermaid";
import TableOfContents from "../UI/TableOfContents.vue";

// manually importing required languages for performances reasons
hljs.registerLanguage('javascript', javascript);
hljs.registerLanguage('java', java);
hljs.registerLanguage('xml', xml);
hljs.registerLanguage('markdown', markdown);
hljs.registerLanguage('json', json);
hljs.registerLanguage('plaintext', plaintext);
hljs.registerLanguage('less', less);
hljs.registerLanguage('bash', bash);
hljs.registerLanguage('awk', awk);
hljs.registerLanguage('css', css);
hljs.registerLanguage('dockerfile', dockerfile);
hljs.registerLanguage('yaml', yaml);
hljs.registerLanguage('ldif', ldif);
hljs.registerLanguage('nginx', nginx);
hljs.registerLanguage('apache', apache);
hljs.registerLanguage('sql', sql);

function getDocumentURL(vm) {
	return new Promise((resolve, reject) => {
		if (vm?.lesson.video) {
			const obj = vm.$smp.getBusinessObject("TrnLsnTranslate");
			obj.get(
				vm.lesson.ltr_id
			).then(item => {
				let url = obj.getFieldDocumentURL("trnLtrVideo", item)
				resolve(url);
			});
		}
	})
}

export default {
	name: "Lesson",
	setup() {
		mermaid.initialize({
			securityLevel: 'high',
		});
        return {
			bg_color: "#ffffff",
			lessonStore: useLessonStore(),
            treeStore: useTreeStore(),
			uiStore: useUiStore()
        }
    },
	components: { Slider, Spinner, EmptyContent, TableOfContents },
	data: () => ({
		alreadyScrolledImages: [],
		lessonViz: 'linear',
		spinner: true,
		videoUrl: null,
		search:null
	}),
	watch: {
		lesson: function (newLesson, oldLesson) {
			if (newLesson.viz === 'TUTO') {
				this.lessonViz = 'default';
			}
		}
	},
	computed: {
		...mapState(useLessonStore, ['lang']),
		...mapState(useLessonStore, ['lesson','lessonImages','lessonTags']),
		...mapState(useTreeStore, ['tree','breadCrumbItems','getLessonFromPath','getCategoryFromPath']),
	},
	methods: {
		addScrollListeners() {
			
			let potentialImages = [];
			document.querySelector(".lesson-block").addEventListener('scroll', (e) => {
				let imageName = null;
				let links = e.target.querySelectorAll("a");
				/* How this feature works : we go through all the a tags of the lesson-content element with #IMG_SCROLL_ & if their lower boundary is
				at a certain fixed point, we tell the slider to go to this image
				*/
				for (const element of links) {
					if (element.hasAttribute("href") && element.getAttribute("href").includes("#IMG_SCROLL_")) {
						if (element.getBoundingClientRect().bottom < e.target.getBoundingClientRect().bottom) {
							imageName = element.getAttribute("href").split("#IMG_SCROLL_")[1];
							if (imageName && !this.alreadyScrolledImages.includes(imageName)) { // We add the imageName to the list
								potentialImages.push(imageName);
								this.alreadyScrolledImages.push(imageName);
							}
						}
					}
				}
				// On affiche la dernière image dans le carousel
				if (this.$refs.slider && potentialImages.length) this.$refs.slider.goToImage(potentialImages[potentialImages.length - 1]);
			});
		},
		addAnchorIcons() {
			if (this.lesson.type !== "lesson" || this.lesson.catPath === "/pages") return;
			const headingTags = document.querySelector(".lesson-block").querySelectorAll("h1, h2, h3, h4, h5, h6");
			for (const heading of headingTags) {
				// create icon tag
				const icon = document.createElement("span");
				icon.className = "icons material-icons";
				icon.textContent = "link";

				// create link
				const headingId = heading.getAttribute("id");
				const link = document.createElement("a");
				link.className = "anchor-link";
				link.setAttribute("href", "#" + headingId);
				link.setAttribute("title", headingId);
				
				// add icon as a child of link, and link as child of the heading
				link.appendChild(icon);
				heading.appendChild(link);
			}
		},
		getAnchorLink(anchorId) {
			return this.$smp.parameters.url + "/lesson" + this.lesson.path
		},
		highlightSearch(searchTerm, content) {
			if (this.lesson.type !== "lesson" || this.lesson.catPath === "/pages" || !content) return;
			// Créer un élément temporaire pour manipuler le contenu HTML
			const tempDiv = document.createElement('div');
			tempDiv.innerHTML = content.innerHTML;

			// Fonction pour encadrer le texte sans toucher aux balises
			const highlightText = (node) => {
				if (node.nodeType === Node.TEXT_NODE) {
					const regex = new RegExp(`(${searchTerm})`, 'gi');
					const newNode = document.createElement('span');
					newNode.innerHTML = this.escapeHtml(node.textContent).replace(regex, '<span class="highlight">$1</span>');
					node.parentNode.replaceChild(newNode, node);
				} else if (node.nodeType === Node.ELEMENT_NODE) {
					// Vérifier si le noeud a des enfants
					node.childNodes.forEach(highlightText);
				}
			};

			// Appliquer la fonction sur tous les nœuds enfants
			tempDiv.childNodes.forEach(highlightText);

			// Remplacer le contenu original par le nouveau contenu
			content.innerHTML = tempDiv.innerHTML;
			
		},
		escapeHtml(unsafe) {
			return unsafe
				.replace(/&/g, "&amp;")
				.replace(/</g, "&lt;")
				.replace(/>/g, "&gt;")
				.replace(/"/g, "&quot;")
				.replace(/'/g, "&#039;")
				.replace(/\(/g, "&#40;")
				.replace(/\)/g, "&#41;")
				;
		},
		unbindMermaidForHljs() {
			const mermaidDivs = document.querySelectorAll(".language-mermaid");
			mermaidDivs.forEach((div) => {
				div.classList.remove("language-mermaid");
				div.classList.add("nohighlight");
				div.classList.add("mermaid");
			});
		},
		addCopyButtons() {
			if (this.lesson.type !== "lesson" || this.lesson.catPath === "/pages") return;
			const codeBlocks = document.querySelector(".lesson-block").querySelectorAll("pre code");
			
			const texts = {
				copy: 'Copy to clipboard',
				copied: 'Copied to clipboard'
			};
			
			for (const codeBlock of codeBlocks) {
				const button = document.createElement("button");
				button.className = "copy-button";
				button.innerHTML = `<span class="material-icons">content_copy</span> ${texts.copy}`;
				button.setAttribute("title", texts.copy);
				
				// Add button to pre element (parent of code)
				const pre = codeBlock.parentElement;
				pre.style.position = 'relative';
				pre.appendChild(button);
				
				button.onclick = () => {
					navigator.clipboard.writeText(codeBlock.textContent);
					button.innerHTML = `<span class="material-icons">check</span> ${texts.copied}`;
					button.classList.add('copied');
					
					setTimeout(() => {
						button.innerHTML = `<span class="material-icons">content_copy</span> ${texts.copy}`;
						button.classList.remove('copied');
					}, 2000);
				};
			}
		},
		openLesson(lesson) {
			this.lessonStore.openLesson({
				smp: this.$smp,
				lesson: lesson
			}).then(() => {
				this.spinner = false;
			}).finally(() => {
				this.addAnchorIcons();
				this.unbindMermaidForHljs();
				hljs.highlightAll();
				this.addCopyButtons();
				
				mermaid.run({
					querySelector: '.mermaid',
				}).catch((e) => {
					console.error('mermaid parsing error: ',e.message);
				})
				if(this.search){
					this.highlightSearch(this.search,document.querySelector('.lesson-html-content'));
				}
				this.fetchVideoUrl();
				
				if (this.$route.hash) {
					const id = this.$route.hash.replace('#', '');
					const el = document.getElementById(id);
					el.scrollIntoView();
				}
			})
		},
		openLessonFromPath() {
			let path = "/" + this.$router.currentRoute.value.params.lessonPath;
			if (path.includes(".md")) {
				const mdLessonPath = path.split(".md");
				path = mdLessonPath[0];
			}
			const lesson = this.getLessonFromPath(path);
			if (!lesson) this.checkUrlRewriting(path);
			else this.openLesson(lesson);
		},
		async checkUrlRewriting(path) {
			const urlRewriting = this.$smp.getBusinessObject("TrnUrlRewriting");
			const array = await urlRewriting.search(
				{"trnSourceUrl": "*" + path}, 
				{inlineDocs: 'infos'}
			);
			if (array[0]) {
				path = array[0].trnDestinationUrl;
			}else{
				path = "/404";
			}
			this.$router.push(path);
		},
		async openPage() {
			this.lessonStore.openPage({
				smp: this.$smp,
				lesson: { row_id: undefined, viz: undefined },
				path: "/" + this.$router.currentRoute.value.params.pagePath
			})
				.catch(async e => {
					await this.$router.push('/404');
				})
				.finally(() => {
					this.spinner = false;
				})
		},
		async openCategory() {
			const cat = this.getCategoryFromPath("/" + this.$router.currentRoute.value.params.categoryPath);
			if (cat) {
				// open first found lesson if it exists, otherwise just open node
				const foundLesson = cat.items.find((item) => item.is_category === false);
				if (foundLesson) {
					const lesson = this.getLessonFromPath(foundLesson.path);
					this.openLesson(lesson);
				} else {
					await this.$router.push('/');
					this.treeStore.OPEN_NODE(cat.path);
				}
			} else await this.$router.push('/404');
		},
		// prevents page reloading on internal URL's
		onHtmlClick(event) {
			const target = event.target;
			const tagName = target.tagName.toLowerCase();

			// Handle anchor tags
			if (tagName === 'a' || (tagName === 'span' && target.parentNode.tagName.toLowerCase() === 'a')) {
				const link = tagName === 'a' ? target : target.parentNode;
				
				// Handle internal anchor links
				if (link.getAttribute('href')?.startsWith('#')) {
					navigator.clipboard.writeText(link.href);
					return;
				}

				// Skip processing for TOC links and download links
				if (link.closest('.toc') || link.hasAttribute('download')) {
					return;
				}

				// Handle internal navigation
				if (link.href?.includes(window.location.origin)) {
					event.stopPropagation();
					event.preventDefault();
					const path = link.getAttribute('href') || link.pathname;
					this.$router.push(path);
				}
				return;
			}

			// Handle images
			if (tagName === 'img') {
				this.uiStore.displayLightBox(target.src);
			}
		},
		async getVideoUrl() {
			if (this.lesson?.video) {
				return await getDocumentURL(this);
			}
			else {
				false;
			}
		},
		fetchVideoUrl() {
			new Promise((resolve, reject) => {
				this.getVideoUrl().then(url => {
					resolve(url);
				}).catch(error => {
					reject(error);
				});
			}).then(url => {
				this.videoUrl = url;
			}).catch(error => {
				console.error(error);
			});
		}
	},
	async created() {
		const searchQuery = this.$route.query.search; // Accéder au paramètre de requête
        if(searchQuery){
			this.search = this.escapeHtml(searchQuery);
		}
		hljs.configure({
			cssSelector: "code"
		});
		this.uiStore.fetchStyle({smp : this.$smp}).finally(() => {
			this.bg_color = this.uiStore.themeValues.neutralColor;
			for (const key in this.uiStore.themeValues.colorAccents) {
				document.documentElement.style.setProperty(key, this.uiStore.themeValues.colorAccents[key]);
			}
		});
		if (Object.prototype.hasOwnProperty.call(this.$router.currentRoute.value.params, "lessonPath") && this.tree.length) {
			this.openLessonFromPath();
		} else if (Object.prototype.hasOwnProperty.call(this.$router.currentRoute.value.params, "pagePath")) {
			this.openPage();
		} else if (Object.prototype.hasOwnProperty.call(this.$router.currentRoute.value.params, "categoryPath")) {
			this.openCategory();
		}
		
	},
	mounted() {
		this.addScrollListeners();
		this.$el.addEventListener("click", this.onHtmlClick);
	},
	beforeUnmount() {
		this.lessonStore.unsetLesson();
	},
	metaInfo() {
		let titleLesson = this.uiStore.getFormattedTitle(this.lesson.title);
		if(!titleLesson) titleLesson = "Docs";
		// Children can override the title.
		return {
			title: titleLesson
		}
	}
};
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style lang="sass" scoped>
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

.lesson
	position: relative
	.lesson-wrapper
		.breadcrumb
			list-style-type: none
			display: flex
			flex-wrap: wrap
			padding: 5px
			padding-bottom: 0.8rem
			border-radius: 0.25rem
			border-bottom: 1px solid #eee
			&__item
				text-transform: uppercase
			&__divider
				margin: $breadcrumb-divider-margin
				text-transform: uppercase
		.tag
			display: flex
			flex-direction: row
			list-style: none
			margin: 15px 0 0 0
			padding-left: 0
			&__item
				background-color: #ddd
				border: none
				color: black
				padding: 8px 13px
				text-align: center
				text-decoration: none
				display: inline-block
				margin: 4px 2px
				border-radius: 10px

.lesson-html-content
	/* :deep is used instead of >>> because we are using sass. It is a deep selector to apply styles to the v-html content*/
	margin-top: 10px
	font-size: 1rem
	@include flex-column-nowrap
	& :deep(details>pre code.hljs)
		display: block
	& :deep(:not(pre) code.hljs)  // targets inline code
		display: inline
		padding: 3px
		border-radius: 3px
		font-family: monospace
	& :deep(code.hljs)
		border-radius: 10px
		margin-bottom: 8px
		font-family: monospace

	& :deep(img)
		max-width: 100%
		height: auto
		overflow-clip-margin: content-box
		overflow: clip
	& :deep(pre)
		margin-bottom: 10px
		position: relative
		
		.copy-button
			position: absolute
			top: 8px
			right: 8px
			padding: 4px
			background: rgba(255, 255, 255, 0.1)
			border: none
			border-radius: 4px
			color: black
			cursor: pointer
			opacity: 0
			transition: opacity 0.2s, background-color 0.2s
			display: flex
			align-items: center
			justify-content: center
			
			&.copied
				background: rgba(40, 167, 69, 0.2)
				opacity: 1
				
			&:hover
				background: rgba(255, 255, 255, 0.2)
				
				&.copied
					background: rgba(40, 167, 69, 0.3)
		
			.material-icons
				font-size: 18px
		
		&:hover
			.copy-button
				opacity: 1

	& :deep(table)
		width: 100%      
		margin: 8px 0
		color: $table-color-text
		border-collapse: collapse
	& :deep(thead)
		padding-top: 10px
		display: table-header-group
		vertical-align: middle
	& :deep(th)
		font-size: map_get($title-sizes, "x-small")
		font-weight: bold
		background-color: $table-color-head-background
		padding: map_get($paddings, "medium")
	& :deep(tr)
		&:nth-child(odd)
			background-color: $table-color-row-background
	& :deep(td)
		padding: map_get($paddings, "small")
		// th, td
		//   padding: map_get($paddings, "small")
	& :deep(th), :deep(td)
		border: solid 1px #E0E0E0

	& :deep(h1), :deep(h2), :deep(h3), :deep(h4), :deep(h5), :deep(h6)
		position: relative
		font-weight: normal
		display: flex
		align-items: center
		&:hover
			.anchor-link
				.icons
					visibility: visible

		.anchor-link
			color: #808080
			display: flex
			align-items: center
			padding: 4px 0 0 10px
			&:hover
				text-decoration: none
				color: #808080

			.icons					
				visibility: hidden
				
	& :deep(h1)
		font-size: map-get($title-sizes, 1)
		color: $color-accent
		border-bottom: solid 1px #E0E0E0
		margin: 0 0 15px 0
	& :deep(h2)
		font-size: map-get($title-sizes, 2)
		color: $color-accent2
		border-bottom: solid 1px #E0E0E0
		margin: 0 0 12px 0
	& :deep(h3)
		font-size: map-get($title-sizes, 3)
		color: $color-accent3
		border-bottom: solid 1px #E0E0E0
		margin: 0 0 8px 0
		.anchor-link
			padding-top: 3px
	& :deep(h4)
		font-size: map-get($title-sizes, 4)
		color: $color-secondary
		border-bottom: solid 1px #E0E0E0
		margin: 0 0 7px 0
	& :deep(h5)
		font-size: map-get($title-sizes, 5)
		color: $color-secondary
	& :deep(h6)
		font-size: map-get($title-sizes, 6)
		color: $color-secondary
	& :deep(p)
		margin-bottom: 10px
	& :deep(ul)
		margin-bottom: 10px
	& :deep(ol)
		list-style-type: decimal
		padding-left: 40px //ol and ul require a certain amount of padding to display the style-type
		//The use of the reset style in app is still to keep though, because it doesn't provoke other issues
		padding-bottom: 10px
	& :deep(li)
		margin-bottom: 5px
	& :deep(.info), :deep(.success), :deep(.warning), :deep(.error)
		border-radius: map-get($radius, regular)
		padding: 0.7rem
		margin: map-get($margins, x-small)
	& :deep(.info), :deep(.note)
		background-color: $color-information
	& :deep(.important)
		background-color: #ffc29e
	& :deep(.success)
		background-color: $color-success
	& :deep(.warning)
		background-color: $color-warning
	& :deep(.error)
		background-color: $color-error

	& :deep(blockquote > p::before)
		content: ''
	& :deep(blockquote > p::after)
		content: ''
	& :deep(blockquote)
		background-color: $color-information
		padding: map_get($paddings, "medium")
		border-radius: map-get($radius, regular)
		margin-bottom: 10px
		p, ul, li
			margin-bottom: 0px
            
	& :deep(strong)
		text-decoration: underline
	& :deep(a)
		color: #007bff
		text-decoration: none
		background-color: transparent
		&:hover
			color: #0062ff
			text-decoration: underline
	& :deep(span)
		.highlight
			background-color: $color-highlight
		
.grid
	position: absolute
	width: 100%
	height: 100%
	display: grid
	grid-template-columns: repeat(2, 50%)
	grid-template-rows: repeat(2, 50%)

.grid-item
	margin: 1em
	background: white
	border-radius: map-get($radius, regular)
	@include box-shadow
	overflow: auto

.lesson-block
	grid-column: 1
	grid-row: 1/3
	padding: 1em
	border-bottom: 1px solid #eee
	background-color: #fff
	padding-bottom: 1em

.default
	.slider-block
		grid-column: 2
		grid-row: 1
	.video-block
		overflow-y: hidden
		grid-column: 2
		grid-row: 2
		.video-wrapper
			@include fillParent()
		.video-player
			@include fillParent()
			object-fit: contain
	.lesson-html-content
		line-height: 1.6

.linear
	.grid
		grid-template-columns: 80% 20%
	.lesson-wrapper
		//@include fillParent()
		padding-right: 25px
		padding-left: 25px
	.lesson-block
		grid-column: 1
	.lesson-html-content
		line-height: 1.7
		& :deep(video)
			max-width: 100%
	.toc-block
		grid-column: 2
		grid-row: 1/3

</style>
