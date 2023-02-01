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
								<div class="ui-button">
									
									<a href="/ui" class="ui-link">
									<button class="button-text">Go to Simplicité UI</button>
								</a>
								</div>
								<div class="text-content">
									<h2 class="text-title">Main features</h2>
									<ul>
										<li>
											<h4 class="sub-title">Categories / Lessons :</h4>
											<p class="text-block">Assign lessons to categories, and set the content in one or more langages.</p>
											<p class="text-block">Both categories and lessons can be published / unpublished.</p>
										</li>
										<li>
											<h4 class="sub-title">Pages : </h4>
											<p class="text-block">This object aims to dispose pages that do not necessarily appear in the tree view.</p>
											<p class="text-block">
											A page must be linked with a lesson (lesson handles the content), its responsibility is to
											serve the content either as a page on the following url <span class="italic">/page/&lt;category-name&gt;/&lt;page-name&gt;</span>
											or as a homepage (limited to 1).
											</p>
											<p class="text-block">
											In order to hide a page from the tree view, unpublish the category linked to the lesson.
											You can also unpublish pages through the linked lesson publish option in which case it won't be served anymore.
											</p>
										</li>
										<li>
											<h4 class="sub-title">Tags :</h4>
											<p class="text-block">
											You can set tags on your lessons. These tags give your users the ability to sort lessons on the frontend.
											</p>
										</li>
										<li>
											<h4 class="sub-title">Site theme :</h4>
											<p class="text-block">Customize the frontend appearence using the Site theme object.</p>
											<p class="text-block">This object gives you the possiblity to change the primary / secondary colors and also the logo of your documentation.</p> 
										</li>
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
	font-size: 1.1rem
	
	.header
		text-align: center
		background-color: #d8d8d8
		border-radius: map-get($radius, "medium")
		margin-bottom: 0.9rem
		.subtitle-text
			padding-bottom: map_get($paddings, "medium")
			padding-top: map_get($paddings, "small")
	
	.content-flex
		line-height: 1.5
		display: flex
		flex-direction: row
		flex-warp: wrap 
		@include box-shadow
		border-radius: map-get($radius, "medium")
		ul
			list-style-type: none

		.content-wrapper
			.ui-button
				float: right
				padding: map_get($paddings, "medium")
				margin-top: map_get($margins, "medium")
				margin-right: map_get($margins, "medium")

				.ui-link
					@include box-shadow
					border: none
					border-radius: 10px
					background-color: #479eff
					//text-align: center
					padding: 15px
					
					&:hover
						background-color: #2859fa
						
					
					.button-text
						font-size: 1.2rem
						color: white
						vertical-align: middle
						cursor: pointer
						
						
			.text-content
				padding: map_get($paddings, "large")
				text-align: justify
				.text-title
					padding-bottom: 5px
				.sub-title
					padding-bottom: map_get($paddings, "small")
					color: #387ED1
				.text-block
					padding-bottom: map_get($paddings, "medium")
					margin-left: 8px
				.italic
					font-style: italic
					color: #4a4a4a

	

</style>