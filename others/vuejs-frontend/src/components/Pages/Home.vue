<template>
	<div class="home">
		<div class ="placeholder">
			<div class="content">
				<div class="content-block">
					<div v-if="lesson.row_id">
						<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
					</div>
					<div v-else-if="gotServerResponse && !lesson.html" class="default-content">
						<div class="header">
							<h1>Welcome</h1>
							<h2>To the default page of the Simplicité documentation module</h2>
							<p class="subtitle-text">This page contains all the informations needed to help you set your own documentation</p>
						</div>
						<div class="content-flex">
							<div class="content-wrapper">
								<div class="text-content" id="left-content">
									<h2 class="text-title">Main features</h2>
									<ul>
										<li>
											<h4>Categories / Lessons :</h4>
											<p>
											Create categories, assign them lessons in one or more langages.
											Both categories and lessons can be published / unpublished.
											</p>
										</li>
										<li>
											<p>
											Pages: the page object aims to dispose pages that do not necessarily appear in the tree view.
											A page must be linked with a lesson (lesson handles the content), its responsibility is to
											serve the content either as page on the following url "/page/&lt;category-name&gt;/&lt;page-name&gt;"
											or as a homepage (limited to 1)
											In order to hide a page from the tree view, unpublish the category linked to the lesson.
											You can also unpublish pages through the linked lesson publish option in which case it won't be served anymore.
											</p>
										</li>
										<li>
											<h4>Tags</h4>
											<p>
											You can set tags on your lessons. These tags give your users the ability to sort the lessons on the frontend.
											</p>
										</li>
										<li>
											<h4>Site theme</h4>
											<p>
											Customize the frontend appearence using the Site theme object. 
											This object gives you the possiblity to change the primary / secondary colors and also the logo of your documentation. 
											</p>
										</li>
									</ul>
								</div>
							</div>
							<div class="content-wrapper">
								<div class="text-content">
									<h2 class="text-title">Project architecture</h2>
									<ul>
										<il>
											<h4>Backend</h4>
											<p>
											Simplicite instance serving a SPA with Vue.js. Good example of an appstorable module
											The backend is a Category / Lesson / Translations model.
											stack + architecture
											</p>
										</il>
									</ul>
								</div>
							</div>
						</div>
					</div>
					<Spinner v-else/>
				</div>
			</div>
		</div>			
	</div>
</template>

<script>
import {mapState} from "vuex";
import Spinner from "../UI/Spinner";
export default {
	components: {Spinner},
		name: "HomePage",
		metaInfo: {
			// Children can override the title.
			title: "Home",
		},
		data: () => ({
			// set to true when server has responded to the homepage request
			// avoid getting a blink of the default page when waiting for a potential homepage
			gotServerResponse: false,  
		}),
		async created() {
			this.$store.dispatch("lesson/openHomePage", {
				smp: this.$smp,
				lesson: {row_id: undefined, viz: undefined},
			}).then(() => {
				this.gotServerResponse = true;
			}).catch(() => {
				console.log('Unable to fetch home page from server. Site will display the default page');
				this.gotServerResponse = true;
			})
		},
		computed: {
			...mapState({
				lesson: state => state.lesson.lesson,
				lessonImages: state => state.lesson.lessonImages,
			}),
		},
		methods: {
			beforeDestroy() {
				this.$store.dispatch('lesson/unsetLesson');
			},
		},
	}
</script>

<style lang="sass" scoped>
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

.home
	position: relative
	.placeholder
		position: absolute
		width: 100%
		height: 100%
		display: grid
		grid-template-columns: repeat(2, 50%)
		grid-template-rows: repeat(2, 50%)
		.content
			grid-row: 1/3
			grid-column: 1/3
			padding: 1em
			margin: 1em
			border-bottom: 1px solid #eee
			background-color: white
			padding-bottom: 1em
			border-radius: map-get($radius, regular)
			@include box-shadow
			overflow: auto
			.content-block
				width: 100%
				height: 100%

.default-content
	padding: map_get($paddings, "medium")
	font-size: 1.3rem
	
	.header
		text-align: center
		background-color: #d8d8d8
		border-radius: map-get($radius, "x-large")
		margin-bottom: 0.9rem
		.subtitle-text
			padding: map_get($paddings, "small")
	
	.content-flex
		line-height: 1.5
		padding-top: 1rem
		display: flex
		flex-direction: row
		flex-warp: wrap 
		ul
			list-style-type: none

		.content-wrapper
			width: 50%
			@include box-shadow
			border-radius: 10px
			margin-left: 10px
			margin-right: 10px
			.text-content
				padding: map_get($paddings, "large")
				text-align: justify
				.text-title
					padding-bottom: 5px
	

</style>