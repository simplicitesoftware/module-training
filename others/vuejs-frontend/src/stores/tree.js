import { defineStore } from 'pinia';
import { useLessonStore } from './lesson';
import { useUiStore } from './ui';
export const useTreeStore = defineStore('treeStore', {
	namespaced: true,
	state: () => ({
		tree: [],
	}),
	getters: {
		breadCrumbItems(state) {
			const lessonStore = useLessonStore();
			let parents = lessonStore.$state.lesson.path.split('/');
			parents.splice(0, 1);
			let cursor = state.tree;
			let path = "";
			let result = [];
			let finish = false;

			parents.forEach(function (val, idx) {
				path += "/" + val;
				if (idx === parents.length - 1) {
					let foundLsn = cursor.find((item) => {
						if(item.path && item.path === path) {
							return true
						}
					})
					if (foundLsn !== undefined) {
						result.push({
							title: foundLsn.title,
							path: foundLsn.path
						});
						finish = true;
					}
				} else {
					let foundCat = cursor.find(item => item.is_category && item.path === path);
					if (foundCat) {
						result.push({
							title: foundCat.title,
							path: foundCat.path
						});
						cursor = foundCat.items
					}

				}
			});
			return finish === true ? result : false;
		},
		getLessonFromPath:
			state => lessonPath => {
				const findLesson = (foundNode, cursor) => {
					if (!foundNode.is_category && foundNode.path === lessonPath) {
						return foundNode;
					} else if (foundNode.is_category && foundNode.items.length > 0) {
						cursor = foundNode.items;
					}

					return cursor;
				}
				return treeExplorer(state.tree, lessonPath, findLesson, null)
			},
		getCategoryFromPath:
			state => catPath => {
				let parents = catPath.split('/');
				parents.splice(0, 1);
				let parentIndex = 0;
				let path = "/" + parents[parentIndex];
				let foundCat = undefined;
				const recursiveCat = function (path, cursor) {
					if (foundCat) return;
					if (path === cursor.path) {
						if (parentIndex === parents.length - 1) {
							foundCat = cursor;
							return;
						} else {
							parentIndex++;
							path += "/" + parents[parentIndex];
							for (const cat of cursor.items.filter((item) => item.is_category === true)) {
								recursiveCat(path, cat);
							}
						}
					}
				}
				// call recursion
				for (const cat of state.tree) {
					recursiveCat(path, cat);
				}
				return foundCat;
			}
	},
	actions: {
		async fetchTree(payload) {
			const treeStore = this;
			// eslint-disable-next-line no-unused-vars
			return new Promise((resolve) => {
				const uiStore = useUiStore();
				
				payload.smp.getExternalObject('TrnTreeService').call(
					{
						array: true,
						lang: uiStore.lang,
					},
					{
						tags: uiStore.selectedTagsRowId
					}
				).then(function (res) {
					let addStateValue = node => {
						node.open = false;
						if (node.is_category) {
							node.items.forEach((node) => {
								if (node.is_category) {
									addStateValue(node);
								}
							});
						}
					}
					res.forEach(addStateValue);
					treeStore.SET_TREE(res);
					resolve(res);
				}).catch((e) => {
					console.error(e);
				})
			})
		},
		SET_TREE(tree) {
			this.tree = tree;
		},
		OPEN_NODE(path) {
			const openNode = (foundNode, cursor) => {
				if (foundNode?.is_category) {
					foundNode.open = true;
					cursor = foundNode.items;
				}
				return cursor;
			}
			treeExplorer(this.tree, path, openNode, "category");
		},
		TOGGLE_NODE_OPEN(targetPath) {
			const toggleNode = (foundNode, cursor) => {
				if (foundNode && foundNode.path == targetPath) {
					foundNode.open = !foundNode.open;
				}
				else if (foundNode?.is_category)
					cursor = foundNode.items;
				return cursor;
			}
			treeExplorer(this.tree, targetPath, toggleNode, "category");
		},
	},
	
});

// tree explorer that takes a function as an argument => used for mutations
function treeExplorer(tree, path, f, searchType) {
	//path = path;
	let parents = path.split('/');
	parents.splice(0, 1);
	let cursor = tree;
	path = "";
	for (let i = 0; i < parents.length; i++) {
		path += "/" + parents[i];
		let foundNode;
		if (searchType === "category") {
			foundNode = cursor.find(item => item.path && item.path === path && item.is_category);
		} else {
			const decomposed = path.split('/');
			decomposed.splice(0, 1);
			if (parents.length === decomposed.length) {
				return cursor.find((item) => {
					if (item.path && item.path === path && !item.is_category) {
						return true;
					}
				});
			} else {
				foundNode = cursor.find((item) => {
					if (item.path && item.path === path) {
						return true;
					}
				});
			}
		}
		cursor = f(foundNode, cursor);
		if (!Array.isArray(cursor)) {
			if (!Object.hasOwn(cursor, "items")) {
				return cursor;
			}
		}
	}
	return cursor;
}
