<template>
	<div class="home">
		<div class ="placeholder">
			<div class="content">
				<div class="content-block">
					<div v-if="lesson.row_id">
						<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
					</div>
					<div v-else-if="gotServerResponse && !lesson.html" class="default-content">
						<h1 class="title">Congratulations!  🥳</h1>
						<div class="content-wrapper">
							<div class="introduction">
								<p>You have successfully installed the documentation module! </p>
								<p>It’s a ready-to use application. Use it to share your information and processes on the web. Do not publish sensible information, or make sure you restrict the access to this website at the network level.</p>
							</div>
							<div class="text-image">
								<div class="default-text">
									<p>Here are some next steps that you might want to take :</p>
									<ul>
										<li>Apply your own branding </li>
										<li>Create some categories and lessons</li>
										<li>Create a custom homepage</li>
									</ul>
									<p>Instructions on how to administrate this website’s content are available on the backoffice, which you can access through the /ui endpoint.</p>
								</div>
								<img class="default-image" src="../../../public/img/homepage-illustration.svg"/>
							</div>
							<div class="button-container">
								<a href="/ui?scope=Formation">
									<button class="backoffice">Access to the administrator’s backoffice</button>
								</a>
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
			order-radius: map-get($radius, regular)
			@include box-shadow
			overflow: auto
			.content-block
				width: 100%
				height: 100%

.default-content
	padding: map_get($paddings, "medium")
	line-height: 1.8rem
	font-size: 1.4rem
	p
		padding-bottom: 20px
	ul
		padding-bottom: 30px
	.title
		text-align: center
		padding: 20px
		padding-bottom: 40px
	.content-wrapper
		padding-left: 22%
		padding-right: 22%
		.introduction
		.text-image
			display: flex
			wrap: column
			.default-text
				flex-direction: column
				margin-top: 10px
			.default-image
				padding-left: 30px
				width: 15em
	.button-container
		padding-top: 40px
		width: 100%
		text-align: center
		.backoffice
			display: inline-block
			background-color:	#6aa84f
			color: white
			border-radius: 15px
			font-size: 1.4rem
			padding: 20px
			&:hover
				cursor: pointer
				@include box-shadow
						
</style>