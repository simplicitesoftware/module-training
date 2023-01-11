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
							<p>Here's all you need to know in order to set your own documentation</p>
						<h3>Goal</h3>
							<p>Easy to deploy and easy to use</p>
							<p>Customisable points such as logo, main color, homepage</p>
						<h3>Project architecture</h3>
							<h4>Backend</h4>
								<p>Simplicite instance serving a SPA with Vue.js</p>
								<p>The backend is a Category / Lesson / Translations model.</p>
								<p>stack + architecture</p>
						<h3>Features</h3>
							<p>Content trough UI</p>
							<p>
								Pages: the page object aims to dispose pages that do not necessarily appear in the tree view.
								In order to hide a page from the tree view, unpublish the category linked to the lesson.
								Pages are served on the "/page/&lt;category-name&gt;/&lt;page-name&gt;" url. 
								You can also unpublish pages through the linked lesson publish option in which case it won't be saved anymore.
							</p>
							<p>Tags, can set tags on lessons, </p>
							<p>Url rewriting</p>
							<p>Site theme</p>
							<p>Tags</p>
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