<template>
	<div class="lesson" :class="lessonViz">
		<div class="grid">
			<div class="grid-item lesson-block">
				<Spinner v-if="spinner"/>
				<div v-else-if="lesson.html" class="lesson-wrapper">
					<ul class="breadcrumb">
						<li class="breadcrumb__item" v-for="(item, index) in breadCrumbItems" :key="index">
							<span>{{item.title}}</span>
							<span class="breadcrumb__divider" v-if="index !== breadCrumbItems.length-1">></span>
						</li>
					</ul>
					<ul v-if="lessonTags.length > 0" class="tag">
						<li class="tag__list" v-for="(tag, index) in lessonTags" :key="index">
							<span class="tag__item">{{tag}}</span>
							<span class="breadcrumb__divider" v-if="index !== breadCrumbItems.length-1"></span>
						</li>
					</ul>
					<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
					<EmptyContent v-else/>
				</div>
			</div>
			<div v-if="lesson.viz !== 'LINEAR'" class="grid-item slider-block">
				<Slider v-if="lessonImages.length" :slides="lessonImages" ref="slider"/>
				<EmptyContent v-else/>
			</div>
			<div v-if="lesson.viz !== 'LINEAR'" class="grid-item video-block">
				<div v-if="lesson" class="video-wrapper">
					<video v-if="videoUrl" class="video-player" controls muted poster="../../../public/media.svg"
							:src="videoUrl" preload="none">
						Sorry, your browser doesn't support embedded videos.
					</video>
					<EmptyContent v-else/>
				</div>
				<Spinner v-else/>
			</div>
		</div>
	</div>
</template>

<script>
	/* eslint-disable no-console,no-unused-vars,no-undef */
	import Spinner from "../UI/Spinner";
	import EmptyContent from "../UI/EmptyContent";
	import Slider from "../UI/Slider";
	import {mapGetters, mapState} from "vuex";
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
			if (vm?.lesson.video){
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
		components: {Slider, Spinner, EmptyContent},
		data: () => ({
			alreadyScrolledImages: [],
			lessonViz: 'linear',
			spinner: true
		}),
		watch: {
			lesson: function (newLesson, oldLesson) {
				if(newLesson.viz === 'TUTO') {
					this.lessonViz = 'default';
				}
			}
		},
		asyncComputed: {
			async videoUrl() {
				if (this.lesson?.video) {
					return await getDocumentURL(this);
				}
				else {
					return false;
				}
			}
		},
		computed: {
			...mapState({
				lesson: state => state.lesson.lesson,
				lessonImages: state => state.lesson.lessonImages,
				lessonTags: state => state.lesson.lessonTags,
				tree: state => state.tree.tree,
			}),
			...mapGetters({
				breadCrumbItems: 'tree/breadCrumbItems',
				getLessonFromPath: 'tree/getLessonFromPath',
				getCategoryFromPath: 'tree/getCategoryFromPath'
			}),
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
					if (potentialImages.length) this.$refs.slider.goToImage(potentialImages[potentialImages.length - 1]);
				});
			},
			openLesson(lesson) {
				this.$store.dispatch("lesson/openLesson", {
					smp: this.$smp,
					lesson: lesson
				}).then(() => {
					this.spinner = false;
				}).finally(() => {
					hljs.highlightAll();
                    if(this.$route.hash) {
						const id = this.$route.hash.replace('#', '');
						const el = document.getElementById(id);
						el.scrollIntoView();
					}
				})
			},
			openLessonFromPath() {
				let path = "/" + this.$router.currentRoute.params.lessonPath;
				if(path.includes(".md")){
					const mdLessonPath = path.split(".md");
					path = mdLessonPath[0];
				}
				const lesson = this.getLessonFromPath(path);
				if(!lesson) this.$router.push('/404');
				else this.openLesson(lesson); 
			},
			async openPage() {
				this.$store.dispatch("lesson/openPage", {
					smp: this.$smp,
					lesson: {row_id: undefined, viz: undefined},
					path: "/" + this.$router.currentRoute.params.pagePath
				}).catch(async e => {
					await this.$router.push('/404');
				});
			},
			async openCategory() {
				const cat = this.getCategoryFromPath("/" + this.$router.currentRoute.params.categoryPath);
				if(cat) {
					if(cat.lessons.length > 0) {
						const lesson = this.getLessonFromPath(cat.lessons[0].path);
						this.openLesson(lesson);
					} else {
						await this.$router.push('/');
						this.$store.commit("tree/OPEN_NODE", cat.path);
					}
				} else await this.$router.push('/404');
			},
			// prevents page reloading on internal URL's
			onHtmlClick(event) {
				if(event.target.tagName.toLowerCase() === 'a') {
					// if the href is served on the same base url
					if(event.target.href.includes(window.location.origin)) {
						event.stopPropagation();
						event.preventDefault();
						this.$router.push(event.target.pathname);
					}
				}
			},
		},
		async created() {
			hljs.configure({
				cssSelector: "code"
			});
			if(Object.prototype.hasOwnProperty.call(this.$router.currentRoute.params, "lessonPath") && this.tree.length) {
				this.openLessonFromPath();
			} else if(Object.prototype.hasOwnProperty.call(this.$router.currentRoute.params, "pagePath")) {
				this.openPage();
			} else if(Object.prototype.hasOwnProperty.call(this.$router.currentRoute.params, "categoryPath")) {
				this.openCategory();
			}
		},
		mounted() {
			this.addScrollListeners();
			this.$el.addEventListener("click", this.onHtmlClick);
		},
		beforeDestroy() {
			this.$store.dispatch('lesson/unsetLesson');
		},
		metaInfo() {
			let lesson = this.lesson;
			// Children can override the title.
			return {
				title: lesson.title
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
	/* ::v-deep is used instead of >>> because we are using sass. It is a deep selector to apply styles to the v-html content*/
	margin-top: 10px
	font-size: 1rem
	@include flex-column-nowrap
	overflow: hidden
	& ::v-deep
		:not(pre) code.hljs  // targets inline code
			display: inline
			padding: 3px
			border-radius: 3px
			font-family: monospace
			
		code.hljs
			border-radius: 10px
			margin-bottom: 8px
			font-family: monospace

		img
			width: 60%
			height: auto
			overflow-clip-margin: content-box
			overflow: clip
		pre
			margin-bottom: 10px

		table
			width: 100%      
			margin: 8px 0 15px 10px
			color: $table-color-text
			border-collapse: collapse
		thead
			padding-top: 10px
			display: table-header-group
			vertical-align: middle
		th
			font-size: map_get($title-sizes, "x-small")
			font-weight: bold
			background-color: $table-color-head-background
			padding: map_get($paddings, "medium")
		tr
			&:nth-child(odd)
				background-color: $table-color-row-background
		td
			padding: map_get($paddings, "small")
		// th, td
		//   padding: map_get($paddings, "small")
		th, td
			border: solid 1px #E0E0E0


		h1, h2, h3, h4, h5, h6
			font-weight: normal
		h1
			font-size: map-get($title-sizes, 1)
			color: #5BC0DE
			border-bottom: solid 1px #E0E0E0
			margin: 0 0 15px 0
		h2
			font-size: map-get($title-sizes, 2)
			color: #5CB85C
			border-bottom: solid 1px #E0E0E0
			margin: 0 0 12px 0
		h3
			font-size: map-get($title-sizes, 3)
			color: #F0AD4E
			border-bottom: solid 1px #E0E0E0
			margin: 0 0 8px 0
		h4
			font-size: map-get($title-sizes, 4)
			color: $color-secondary
			border-bottom: solid 1px #E0E0E0
			margin: 0 0 7px 0
		h5
			font-size: map-get($title-sizes, 5)
			color: $color-secondary
		h6
			font-size: map-get($title-sizes, 6)
			color: $color-secondary
		p
			margin-bottom: 10px
		ul
			margin-bottom: 10px
		ol
			list-style-type: decimal
			padding-left: 40px //ol and ul require a certain amount of padding to display the style-type
			//The use of the reset style in app is still to keep though, because it doesn't provoke other issues
			padding-bottom: 10px
		li
			margin-bottom: 5px
		.info, .success, .warning, .error
			border-radius: map-get($radius, regular)
			padding: 0.7rem
			margin: map-get($margins, x-small)
		.info
			ackground-color: $color-information
		.success
			background-color: $color-success
		.warning
			background-color: $color-warning
		.error
			background-color: $color-error

		blockquote > p::before
			content: ''
		blockquote > p::after
			content: ''
		blockquote
			background: #ffe9e3
			padding: map_get($paddings, "medium")
			border-radius: map-get($radius, regular)
		strong
			text-decoration: underline
		a
			color: #007bff
			text-decoration: none
			background-color: transparent
			&:hover
				color: #0062ff
				text-decoration: underline

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
	.lesson-wrapper
		//@include fillParent()
		padding-right: 20%
		padding-left: 2%
	.lesson-block
		grid-column: 1/3
	.lesson-html-content
		line-height: 1.7

</style>
