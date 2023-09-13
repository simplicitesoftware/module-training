import { SET_DRAWER_STATE, SET_LIGHT_BOX_IMAGE, SET_LIGHT_BOX_VISIBILITY, TOGGLE_LANG, TOGGLE_MODAL_STATE, SET_TAG_LIST, TOGGLE_TAG_UI_SELECTION, SET_TAG_LIST_SELECTION, DEFAULT_TAG_LIST, TAG_MODAL_CANCELLATION, SET_STYLE } from "../mutation-types";

export default {
	namespaced: true,
	state: {
		isDrawerOpen: true,
		isLightBoxVisible: false,
		lightBoxImageSrc: '',
		langIndex: 1,
		langList: ['FRA', 'ENU'],
		tagList: [],
		tagCache: [], // cache used to apply correct values after toggle lang
		isModalOpen: false,
		themeValues: { primaryColor: "#274E13", secondaryColor: "#6AA84F", iconUrl: undefined }
	},
	actions: {
		toggleDrawer({ commit, state }) {
			commit(SET_DRAWER_STATE, !state.isDrawerOpen);
		},
		displayLightBox({ commit }, imageSrc) {
			commit(SET_LIGHT_BOX_IMAGE, imageSrc);
			commit(SET_LIGHT_BOX_VISIBILITY, true);
		},
		hideLightBox({ commit }) {
			commit(SET_LIGHT_BOX_VISIBILITY, false);
		},
		async toggleLang({ dispatch, commit, rootState }, payload) {
			commit(TOGGLE_LANG);
			await dispatch('tree/fetchTree', payload, { root: true });
			await dispatch('fetchTags', payload);
			// TODO only necessary if we are on a lesson path
			if (rootState.lesson.lesson != {}) {
				payload.lesson = rootState.lesson.lesson;
				await dispatch('lesson/openLesson', payload, { root: true });
			}
		},
		async fetchTags({ commit, rootGetters }, payload) {
			return new Promise((resolve, reject) => {
				payload.smp.getExternalObject('TrnTagService').call(
					{
						array: true,
						lang: rootGetters['ui/lang'],
					},
				).then(res => {
					if (res) {
						commit(SET_TAG_LIST, res.map(tag => (
							{
								rowId: tag.row_id,
								code: tag.code,
								display_value: tag.display_value,
								uiSelected: false,
								selected: false,
							}
						)));
						resolve();
					} else {
						reject("Cannot fetch tags");
					}
				}).catch(e => {
					console.log(e);
				})
			})
		},
		// eslint-disable-next-line no-unused-vars
		async fetchStyle({ commit }, payload) {
			return new Promise((resolve) => {
				const siteTheme = payload.smp.getBusinessObject('TrnSiteTheme')
				siteTheme.search().then(async (res) => {
					if (res[0]) {
						const iconUrl = siteTheme.getFieldDocumentURL("trnThemeIcon", res[0]);
						const themeValues = {
							primaryColor: res[0].trnThemeColor,
							secondaryColor: res[0].trnThemeSecondaryColor,
							iconUrl: iconUrl
						}
						commit(SET_STYLE, themeValues)
					}
					resolve();
				}).catch(e => console.log(e));
			});
		}
	},
	getters: {
		lang: state => state.langList[state.langIndex],
		// ElasticSearch research lang format
		langEsFormat: state => state.langList[state.langIndex].toLowerCase(),
		selectedTagsRowId: state => state.tagList.filter(tag => tag.selected).map(filteredTag => {
			return { row_id: filteredTag.rowId };
		}),
		isTagDefined: state => state.tagList.length > 0 ? true : false,
		isSortedByTag: state => state.tagList.filter(tag => tag.selected).length > 0 ? true : false,
		getTagDisplayWithRowId: state => (rowId) => {
			return state.tagList.find(tag => tag.rowId === rowId).display_value;
		}
	},
	mutations: {
		[SET_DRAWER_STATE](state, choice) {
			state.isDrawerOpen = choice;
		},
		[SET_LIGHT_BOX_IMAGE](state, imageSrc) {
			state.lightBoxImageSrc = imageSrc;
		},
		[SET_LIGHT_BOX_VISIBILITY](state, visibility) {
			state.isLightBoxVisible = visibility;
		},
		[TOGGLE_LANG](state) {
			state.langIndex = state.langIndex < state.langList.length - 1 ? state.langIndex + 1 : 0;
		},
		[TOGGLE_MODAL_STATE](state) {
			state.isModalOpen = !state.isModalOpen;
		},
		[SET_TAG_LIST](state, list) {
			list.forEach(tag => {
				if (state.tagCache.includes(tag.rowId)) {
					tag.selected = true;
					tag.uiSelected = true;
				}
			})
			state.tagList = list;
		},
		[TOGGLE_TAG_UI_SELECTION](state, tagIndex) {
			state.tagList[tagIndex].uiSelected = !state.tagList[tagIndex].uiSelected;
		},
		[SET_TAG_LIST_SELECTION](state) {
			state.tagList.forEach(tag => {
				tag.selected = tag.uiSelected;
				if (tag.selected) state.tagCache.push(tag.rowId)
			});
		},
		// if tag modal is cancelled, set the ui state according to actual selected elements
		[TAG_MODAL_CANCELLATION](state) {
			state.tagList.forEach(tag => {
				if (!tag.selected) {
					tag.uiSelected = false;
				}
			})
		},
		[DEFAULT_TAG_LIST](state) {
			state.tagList.forEach(tag => {
				tag.uiSelected = false;
				tag.selected = false;
			})
			state.tagCache = [];
		},
		[SET_STYLE](state, themeValues) {
			state.themeValues = themeValues;
		}
	}
}
