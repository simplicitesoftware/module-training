<template>
	<div class="home">
		<div class ="placeholder">
			<div class="content">
				<div class="content-block">
					<div v-if="lesson.row_id">
						<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
					</div>
					<div v-else-if="gotServerResponse && !lesson.html">
						<h1>Welcome</h1>
						<h2>To the default page of the Simplicité documentation module</h2>
							<p>You'll find on this page all the informations needed to help you set your own documentation</p>
							<p>Is a great example of a simplicité instance serving a Vue.js frontend ???</p>
						<h3>Main features</h3>
							<ul>
								<li>
									<p>Categories / Lessons :</p>
									Create categories, assign them lessons, set the lessons content in one or more langages.
									Both categories and lessons can be published / unpublished.
								</li>
								<li></li>
							</ul>
							<p></p>
							<p>
								Pages: the page object aims to dispose pages that do not necessarily appear in the tree view.
								A page must be linked with a lesson (lesson handles the content), the page responsibility is to
								serve the content either as page on the following url "/page/&lt;category-name&gt;/&lt;page-name&gt;"
								or as a homepage (limited to 1)
								In order to hide a page from the tree view, unpublish the category linked to the lesson.
								You can also unpublish pages through the linked lesson publish option in which case it won't be served anymore.
							</p>
						<h4>Tags</h4>
							<p>
								You can set tags on your lessons. These tags give your users the ability to sort the lessons on the frontend.
							</p>
						<h4>Site theme</h4>
							<p>
								Customize the frontend appearence using the Site theme object. 
								This object gives you the possiblity to change the primary / secondary colors and also the logo of your documentation. 
							</p>
						<h3>Project architecture</h3>
							<h4>Backend</h4>
								<p>Simplicite instance serving a SPA with Vue.js</p>
								<p>The backend is a Category / Lesson / Translations model.</p>
								<p>stack + architecture</p>
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
</style>