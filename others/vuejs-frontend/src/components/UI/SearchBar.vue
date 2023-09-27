<template>
	<div id="SearchBar"  v-click-outside="hideSuggestions">
		<div class="searchElement">
			<input class="searchbar" @input="queryIndex" v-model="inputValue" type="text" :placeholder="searchbarPlaceHolder"/>
			<div @click="searchIconClick" class="searchbar-logo-container" :style="{[`background-color`]: `${themeValues.primaryColor}`}">
				<span class="material-icons searchbar-logo">
					search
				</span>
			</div>
		</div>
		<div class="suggestionRelative">
			<div v-if="isSugOpen" class="result-list-container">
                <div v-if="suggestions">
                    <div v-for="suggestion in suggestions || []" :key="suggestion.row_id">
                        <SuggestionItem
                            :suggestion="suggestion"
                            :inputValue="inputValue"
                            @suggestionItemSelected="suggestionSelected"
                        />
                    </div>
                </div>
				
				<div class="result-list-empty" v-else>
					{{emptyResult}}
				</div>
			</div>
		</div>
	</div>
</template>


<script>

import SuggestionItem from "./SuggestionItem/SuggestionItem.vue";
import {mapGetters} from "vuex";

export default {
	
	name: "SearchBar",
	components :{
		SuggestionItem
	},
	props: {
		themeValues: Object
	},
	data: function() {
		return {
			inputValue:'',
			searchUsed: false,
			currentLangSearchFields : [
				{
					field: 'title',
					weight: '5'
				},
				{
					field: 'raw_content',
					weight: '1'
				}
			],
			anySearchFields: [
				{
					field: 'title_any',
					weight: '5'
				},
				{
					field: 'raw_content_any',
					weight: '1'
				},
                {
                    field: 'title',
                    weight: '5'
                },
                {
                    field: "posts.content",
                    weight: '1'
                }
            ],
			hover: false,
			isSugOpen:false,
			queryInput:'',
			result:'',
			suggestions:null,
			sugsExist:false,
			timeout:null,
            contentMaxLength: 500
		}
	},
	computed:{
		...mapGetters({
			lang: 'ui/lang',
			langEsFormat: 'ui/langEsFormat'
		}),
		// getsearchFields : function() {
		// 	return this.getQueryFormat(this.currentLangSearchFields, true).concat(this.getQueryFormat(this.anySearchFields, false));
		// },
		// gethighlightFields : function() {
		// 	const obj = new Object();
		// 	for(const f of this.currentLangSearchFields) {
		// 		obj[f.field+"_"+this.langEsFormat] = {};
		// 	}
        //     for(const f of this.anySearchFields) {
        //         obj[f.field] = {};
        //     }
		// 	return obj;
		// },
		searchbarPlaceHolder : function(){
			return "FRA" == this.lang ? "Rechercher" : "Search"
		},
		emptyResult : function(){
			return "FRA" == this.lang ? "Aucun résultat de recherche." : "No results found."
		},
		...mapGetters({
			getLessonFromPath: 'tree/getLessonFromPath',
		}),
	},
	methods: {
		// getQueryFormat(fields, formatLang) {
		// 	return fields.map((f) => f.field+(formatLang ? "_"+this.langEsFormat : "")+"^"+f.weight);
		// },
		hideSuggestions(){
			this.isSugOpen = false;
		},
		searchIconClick() {
			this.isSugOpen = true;
		},
		suggestionSelected(suggestion) {
			this.isSugOpen = false;
			this.inputValue = '';
            if(suggestion.type === "lesson") {
                const lsn = this.getLessonFromPath(suggestion.path);
                if (lsn && !lsn.is_category) this.$router.push('/lesson' + suggestion.path).catch(err => console.error(err));
                else this.$store.commit("tree/OPEN_NODE", suggestion.path);
            } else if(suggestion.type === "discourse") {
                window.open(suggestion.path);
            } else if(suggestion.type === "simplicite") {
                this.$router.push('/lesson' + suggestion.path).catch(err => console.log(err));
            }
		},
		queryIndex(){
            if(this.inputValue === "") {
                this.isSugOpen = false;
                return;
            }
            this.callSearchService(this.inputValue);
		},
        callSearchService(inputValue) {
			const headers = new Headers();
            headers.append("Authorization", this.$smp.getBearerTokenHeader());
			headers.append("Content-Type", "application/json");
			
			const requestOptions = {
				method: 'GET',
				headers: headers,
				redirect: 'follow'
			};
			fetch(this.$smp.parameters.url+"/api/ext/TrnSearchService/?query="+inputValue+"&lang="+this.lang, requestOptions)
			.then(response => response.json())
			.then((json) => {
                if(json.length > 0) {
                    this.suggestions = json;
                    this.isSugOpen = true;
                }
				else{
					this.suggestions = null;
				}
			})
			.catch(error => console.log('error', error));
        }

		// searchElasticSearch(inputValue){
			
		// 	const myHeaders = new Headers();
		// 	if(process.env.NODE_ENV !== "local") {
		// 		const authent = Buffer.from(this.$ES_CREDENTIALS, 'utf8').toString('base64');
		// 		myHeaders.append("Authorization", "Basic "+authent);
		// 	}
			
		// 	myHeaders.append("Content-Type", "application/json");
		// 	myHeaders.append("Origin", this.$ES_INSTANCE);
			
		// 	// any results are not returned
		// 	const json = {
		// 		"query": {
		// 			"multi_match": {
		// 				"type": "phrase_prefix",
		// 				"query": inputValue,
		// 				"fields": this.getsearchFields
		// 			}
		// 		},
		// 		"highlight": {
		// 			"fields": this.gethighlightFields,
		// 			"fragment_size":500
		// 		},
		// 		"size": 10
		// 	};
			
		// 	const raw = JSON.stringify(json);
			
		// 	const requestOptions = {
		// 		method: 'POST',
		// 		headers: myHeaders,
		// 		body: raw,
		// 		redirect: 'follow'
		// 	};
			
		// 	const searchUrl = this.$ES_INSTANCE+"/"+this.$ES_INDEX+"/_search";
			
		// 	fetch(searchUrl, requestOptions)
		// 	.then(response => response.json())
		// 	.then(json => {
		// 		if(json.hits) {
		// 			const hits = json.hits.hits
		// 			if(hits.length != 0){
		// 				this.suggestions = hits;
		// 				this.isSugOpen = true;
		// 			}
		// 			else{
		// 				this.suggestions = null;
		// 			}
		// 		}
		// 	})
		// 	.catch(error => console.log('error', error));
		// },
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
