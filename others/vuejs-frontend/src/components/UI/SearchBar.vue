<template>
	<div id="SearchBar" v-click-outside="hideSuggestions">
		<div class="searchElement">
			<input class="searchbar" @input="queryIndex" v-model="inputValue" type="text" :placeholder="searchbarPlaceHolder"
				v-on:keyup.enter="openAdvancedSearch" />
			<div @click="openAdvancedSearch" class="searchbar-logo-container"
				:style="{ [`background-color`]: `${themeValues.primaryColor}` }">
				<span class="material-icons searchbar-logo">
					search
				</span>
			</div>
		</div>
		<div class="suggestionRelative">
			<div v-if="isSugOpen" class="result-list-container">
				<div v-if="suggestions">
					<div v-for="suggestion in suggestions || []" :key="suggestion.id">
						<SuggestionItem :suggestion="suggestion" @suggestionItemSelected="suggestionSelected" />
					</div>
				</div>

				<div class="result-list-empty" v-else>
					{{ emptyResult }}
				</div>
			</div>
		</div>
	</div>
</template>


<script>

import SuggestionItem from "./SuggestionItem/SuggestionItem.vue";
import s from "../../shared";
import { mapState } from "pinia";
import { useUiStore } from "../../stores/ui";

export default {

	name: "SearchBar",
	components: {
		SuggestionItem
	},
	props: {
		themeValues: Object
	},
	data: function () {
		return {
			inputValue: '',
			hover: false,
			isSugOpen: false,
			result: '',
			suggestions: null,
		}
	},
	computed: {
		...mapState(useUiStore, ['lang', 'langEsFormat','getLessonFromPath']),
		searchbarPlaceHolder: function () {
			return "FRA" == this.lang ? "Rechercher" : "Search"
		},
		emptyResult: function () {
			return "FRA" == this.lang ? "Aucun résultat de recherche." : "No results found."
		},
		
	},
	methods: {
		hideSuggestions() {
			this.isSugOpen = false;
		},
		openAdvancedSearch() {
			this.hideSuggestions();
			this.$router.push('/search/' + this.inputValue).catch(err => console.error(err));
		},
		suggestionSelected(suggestion) {
			this.isSugOpen = false;
			this.inputValue = '';
			if (suggestion.type === "lesson") {
				this.$router.push('/lesson' + suggestion.path).catch(err => console.error(err));
			} else if (suggestion.type === "discourse") {
				window.open(suggestion.url);
			} else if (suggestion.type === "simplicite") {
				this.$router.push('/lesson' + suggestion.path).catch(err => console.log(err));
			} else if (suggestion.type === "page") {
				this.$router.push('/page' + suggestion.path).catch(err => console.log(err));
			}
		},
		async queryIndex() {
			if (this.inputValue === "") {
				this.isSugOpen = false;
				return;
			} else {
				this.isSugOpen = true;
			}
			try{
				const res = await s.callSearchService(this.$smp.parameters.url, this.$smp.getBearerTokenHeader(), this.inputValue, this.lang, [], 0);
				console.log(res);
				this.suggestions = res.results;
			}catch(err){
				console.log("error in queryIndex: ", err);
			}
		},

	}
};

</script>

<style scoped lang="sass">
@import "../../assets/sass/variables"
@import "../../assets/sass/mixins"

#SearchBar
	min-width: 40rem

.searchElement
	display: flex
	width: 50vw

.searchbar
	flex-basis: 100%
	padding-left: 10px
	font-size: 1rem

.searchbar-logo-container
	display: flex
	justify-content: center
	align-items: center
	flex-direction: column
	padding: .5rem
	&:hover
		background-color: transparentize(white, 0.9)
		cursor: pointer

.searchbar-logo

.suggestionRelative
	position: relative

.datasearch
	display: flex
	flex-direction: column

.result-list-container
	display: flex
	flex-direction: column
	border-top: 1.5px solid #ccc
	border-left: 1.5px solid #ccc
	border-right: 1.5px solid #ccc
	border-bottom: 1.5px solid #ccc
	background-color: #fafafa
	border-radius: 0 0 8px 8px
	position: absolute
	z-index: 1
	overflow: scroll
	max-height: 80vh
	width: 50vw
	box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2), 0 6px 20px 0 rgba(0, 0, 0, 0.19)

.result-list-empty
	color: black
	padding: .5rem

</style>
