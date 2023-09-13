import { SET_TREE, OPEN_NODE, TOGGLE_NODE_OPEN } from "../mutation-types";

export default {
	namespaced: true,
	state: {
		tree: [],
	},
	getters: {
		breadCrumbItems(state, getters, rootState) {
			let parents = rootState.lesson.lesson.path.split('/');
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
							for (const cat of cursor.categories) {
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
		async fetchTree({ commit, rootGetters }, payload) {
			// eslint-disable-next-line no-unused-vars
			return new Promise((resolve) => {

				payload.smp.getExternalObject('TrnTreeService').call(
					{
						array: true,
						lang: rootGetters['ui/lang'],
					},
					{
						tags: rootGetters['ui/selectedTagsRowId']
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
					commit(SET_TREE, res);
					resolve(res);
				}).catch((e) => {
					console.error(e);
				})
			})
		},
	},
	mutations: {
		[SET_TREE](state, tree) {
			state.tree = tree;
		},
		[OPEN_NODE](state, path) {
			const openNode = (foundNode, cursor) => {
				if (foundNode?.is_category) {
					foundNode.open = true;
					cursor = foundNode.items;
				}
				return cursor;
			}
			treeExplorer(state.tree, path, openNode, "category");
		},
		[TOGGLE_NODE_OPEN](state, targetPath) {
			const toggleNode = (foundNode, cursor) => {
				if (foundNode && foundNode.path == targetPath) {
					foundNode.open = !foundNode.open;
				}
				else if (foundNode?.is_category)
					cursor = foundNode.items;
				return cursor;
			}
			treeExplorer(state.tree, targetPath, toggleNode, "category");
		}
	},
}

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
