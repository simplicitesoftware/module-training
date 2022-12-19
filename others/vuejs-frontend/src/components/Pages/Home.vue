<template>
	<div class="wrapper">
		<div v-if="lesson.row_id">
			<div class="lesson-html-content" v-if="lesson.html" v-html="lesson.html"></div>
		</div>
		<div v-else>
			<h1>Welcome on the Simplicité doc module</h1>
			<p>Description of the module</p>
			<p>How to use it</p>
			<p>How to set a custom home page</p>
		</div>
	</div>
</template>

<script>
	import {mapState} from "vuex";
	export default {
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

</style>