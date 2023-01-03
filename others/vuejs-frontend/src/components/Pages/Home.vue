<template>
	<div class="home">
		<div class ="placeholder">
			<div class="content">
				<div class="content-block">
					<div v-if="lesson.row_id">
						<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
					</div>
					<div v-else>
						<div class="banner">
							<div class="banner-wrapper">
								<img
								src="../../../public/img/illustration_training_Simplicite.png"
								class="illustration"
								/>
								<div class="text-content">
									<h1 class="heading">
										<strong>Documentation</strong>
									</h1>
									<h2 class="sub-heading">
										All the developer resources accessible from one single place.
									</h2>
								</div>
							</div>
						</div>
						<!-- Content -->
						<div class="content">
							<div class="content__description">
								<div class="content__description__wrapper">
									<h2 class="title">What is the Simplicité low-code platform ?</h2>
									<div class="text">
										<img
										src="../../../public/img/Schema_Simplicite.png"
										class="text__image"
									/>
									<div class="row">
										<p class="p-style">
											Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
											eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut
											enim ad minim veniam, quis nostrud exercitation ullamco laboris
											nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor
											in reprehenderit in voluptate velit esse cillum dolore eu fugiat
											nulla pariatur. Excepteur sint occaecat cupidatat non proident,
											sunt in culpa qui officia deserunt mollit anim id est laborum.
											<br>
											<br>
											Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do
											eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut
											enim ad minim veniam, quis nostrud exercitation ullamco laboris
											nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor
											in reprehenderit in voluptate velit esse cillum dolore eu fugiat
											nulla pariatur. Excepteur sint occaecat cupidatat non proident,
											sunt in culpa qui officia deserunt mollit anim id est laborum.
										</p>
									</div>
								</div>
							</div>
						</div>
						<div class="content__link_wrapper">
							<a class="link-item">
								<div class="link-item__content">
									<span class="material-icons icons">link</span>
									<p class="link-text">Deploy your sandbox and start building !</p>
								</div>
							</a>
							<a class="link-item" href="https://community.simplicite.io/">
								<div class="link-item__content">
									<span class="material-icons icons">link</span>
									<p class="link-text">Community, the Simplicité support, to find all your answers</p>
								</div>
							</a>
							<a class="link-item" href="https://docs.simplicite.io/documentation/">
								<div class="link-item__content">
									<span class="material-icons icons">link</span>
									Technical documentation
								</div>
							</a>
							<a class="link-item" href="https://docs.simplicite.io/5/">
								<div class="link-item__content">
									<span class="material-icons icons">link</span>
									<p class="link-text">Javadocs, JSDocs, Release Notes...</p>
								</div>
							</a>
							<a class="link-item">
								<div class="link-item__content">
									<span class="material-icons icons">link</span>
									<p class="link-text">Docker Registry</p>
								</div>
							</a>
						</div>
					</div>
				</div>
			</div>
		</div>			
	</div>
</div>
</template>

<script>
import {mapState} from "vuex";
export default {
  components: {},
		name: "HomePage",
		metaInfo: {
			// Children can override the title.
			title: "Home",
		},
		async created() {
			this.$store.dispatch("lesson/openHomePage", {
				smp: this.$smp,
				lesson: {row_id: undefined, viz: undefined},
			});
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
	width: 100%
	height: 100%
	position: relative
	.placeholder
		position: absolute
		width: 100%
		height: 100%
		display: grid
		grid-template-rows: repeat(2, 50%)
		.content
			grid-row: 1/3
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
				.banner
				@include background-image("../../../public/img/Home-training-center.jpg", 25%, $color-code-background)
				border-radius: 10px
				.banner-wrapper
					display: flex
					flex-direction: row
					color: white
					text-align: center
					padding-bottom: 20px
					.illustration
						padding-left: 15%
					.text-content
						font-size: 20px
						padding-left: 60px
						text-align: left
						align-self: center
						max-width: 600px
						.heading
						.sub-heading
							padding-top: 10px

				.content
					display: flex
					flex-direction: row
					padding: 2%
					&__description
						@include box-shadow
						border-radius: 15px
						width: 60%
						&__wrapper
							padding: 3%
							.title
								text-align: center
							.text
								padding-top: 4%
								&__image
									float: right
					&__link_wrapper
						width: 40%
						padding-left: 4%
						padding-right: 2%
						display: flex
						flex-direction: column
						.link-item
							@include box-shadow
							width: 100%
							height: 3.5em
							border-radius: 10px
							font-size: 1vw
							margin-bottom: 2em
							align-self: center
							color: inherit
							text-decoration: inherit
							&:hover
								cursor: pointer
							&__content
								display: flex
								flex-direction: row
								padding-top: 16px
								padding-left: 15px
								.icons
										
								.link-text
				
</style>