import { defineStore } from 'pinia';
import { useLessonStore } from './lesson';
import { useTreeStore } from './tree';

export const useUiStore = defineStore('uiStore', {
	namespaced: true,
	state: () => ({
		isDrawerOpen: true,
		isLightBoxVisible: false,
		lightBoxImageSrc: '',
		langIndex: 1,
		langList: ['FRA', 'ENU'],
		tagList: [],
		tagCache: [], // cache used to apply correct values after toggle lang
		isModalOpen: false,
		themeValues: { primaryColor: "#274E13", secondaryColor: "#6AA84F", iconUrl: undefined }
	}),
	actions: {
		toggleDrawer() {
			this.SET_DRAWER_STATE(!this.isDrawerOpen);
		},
		displayLightBox(imageSrc) {
			this.SET_LIGHT_BOX_IMAGE(imageSrc);
			this.SET_LIGHT_BOX_VISIBILITY(true);
		},
		hideLightBox() {
			this.SET_LIGHT_BOX_VISIBILITY(false);
		},
		async toggleLang(payload) {
			this.TOGGLE_LANG();
			const lessonStore = useLessonStore();
			const treeStore = useTreeStore();
			await treeStore.fetchTree(payload, { root: true });
			await this.fetchTags(payload);
			// TODO only necessary if we are on a lesson path
			if (lessonStore.$state.lesson != {}) {
				payload.lesson = lessonStore.$state.lesson ;
				await lessonStore.openLesson(payload, { root: true });
			}
		},
		async fetchTags(payload) {
			return new Promise((resolve, reject) => {
				payload.smp.getExternalObject('TrnTagService').call(
					{
						array: true,
						lang: this.lang,
					},
				).then(res => {
					if (res) {
						this.SET_TAG_LIST(res.map(tag => (
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
		async fetchStyle(payload) {
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
						this.SET_STYLE(themeValues)
					}
					resolve();
				}).catch(e => console.log(e));
			});
		},
		//Mutations
		SET_DRAWER_STATE(choice) {
			this.isDrawerOpen = choice;
		},
		SET_LIGHT_BOX_IMAGE(imageSrc) {
			this.lightBoxImageSrc = imageSrc;
		},
		SET_LIGHT_BOX_VISIBILITY(visibility) {
			this.isLightBoxVisible = visibility;
		},
		TOGGLE_LANG() {
			this.langIndex = this.langIndex < this.langList.length - 1 ? this.langIndex + 1 : 0;
		},
		TOGGLE_MODAL_STATE() {
			this.isModalOpen = !this.isModalOpen;
		},
		SET_TAG_LIST(list) {
			list.forEach(tag => {
				if (this.tagCache.includes(tag.rowId)) {
					tag.selected = true;
					tag.uiSelected = true;
				}
			})
			this.tagList = list;
		},
		TOGGLE_TAG_UI_SELECTION(tagIndex) {
			this.tagList[tagIndex].uiSelected = !this.tagList[tagIndex].uiSelected;
		},
		SET_TAG_LIST_SELECTION() {
			this.tagList.forEach(tag => {
				tag.selected = tag.uiSelected;
				if (tag.selected) this.tagCache.push(tag.rowId)
			});
		},
		// if tag modal is cancelled, set the ui state according to actual selected elements
		TAG_MODAL_CANCELLATION() {
			this.tagList.forEach(tag => {
				if (!tag.selected) {
					tag.uiSelected = false;
				}
			})
		},
		DEFAULT_TAG_LIST() {
			this.tagList.forEach(tag => {
				tag.uiSelected = false;
				tag.selected = false;
			})
			this.tagCache = [];
		},
		SET_STYLE(themeValues) {
			this.themeValues = themeValues;
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
});
