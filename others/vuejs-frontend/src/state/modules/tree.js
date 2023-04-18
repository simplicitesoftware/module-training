import {SET_TREE,OPEN_NODE,TOGGLE_NODE_OPEN} from "../mutation-types";

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
        let foundCat = cursor.find(item => item.is_category && item.path === path);
        if (foundCat !== undefined) {
          result.push({
            title: foundCat.title,
            path: foundCat.path
          });
          if (idx === parents.length - 2)
            cursor = foundCat.lessons;
          else
            cursor = foundCat.categories;
        } else if (idx === parents.length - 1) {
          let foundLsn = cursor.find(item => item.path && item.path === path);
          if (foundLsn !== undefined) {
            result.push({
              title: foundLsn.title,
              path: foundLsn.path
            });
            finish = true;
          }
        }
      });
      return finish === true ? result : false;
    },
    getLessonFromPath:
      state => lessonPath => {
        // "/tutorial/configuration/creermodule" => ["", "configuration", "creermodule"]
        let parents = lessonPath.split('/'); 
        // remove first element (empty string)
        parents.splice(0, 1); 
        // initiate cursor at root of tree
        let cursor = state.tree; 
        let path = "";
        let foundLsn = undefined;
        parents.forEach(function (val, idx) {
          path += "/" + val;
          if(idx === parents.length - 1){ // last item => FIND LESSON
            foundLsn = cursor.find(item => item.path && item.path === path);
          }
          else {
            let foundCat = cursor.find(item => item.path && item.path === path);
            if(foundCat !== undefined && idx === parents.length - 2) // before last item => SWITCH TO LESSON LIST
              cursor = foundCat.lessons;
            else if(foundCat !== undefined) // else continue with categories
              cursor = foundCat.categories;
            // else lesson will be undefined
          }
        });
        return foundLsn;
      },
    getCategoryFromPath:
      state => catPath => {
      let parents = catPath.split('/');
      parents.splice(0, 1);
      let parentIndex = 0;
      let path = "/" + parents[parentIndex];
      let foundCat = undefined;

      const recursiveCat = function(path, cursor) {
        if(foundCat) return;
        if(path === cursor.path) {
          if(parentIndex === parents.length - 1) {
            foundCat = cursor;
            return;
          } else {
            parentIndex++;
            path += "/" + parents[parentIndex];
            for(const cat of cursor.categories) {
              recursiveCat(path, cat);
            }
          }
        }
      }
      // call recursion
      for(const cat of state.tree) {
        recursiveCat(path, cat);  
      }
      return foundCat;
    }
  },
  actions: {
    async fetchTree({commit, rootGetters}, payload) {
      // eslint-disable-next-line no-unused-vars
      return new Promise((resolve) => {
        
        payload.smp.getExternalObject('TrnTreeService').call(
          {
            array:true,
            lang:rootGetters['ui/lang'],
          },
          {
            tags:rootGetters['ui/selectedTagsRowId']
          }
        ).then(function(res){
          let addStateValue = node => {
            node.open = false;
            if(node.is_category)
              node.categories.forEach(addStateValue);
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
    [OPEN_NODE](state, path){
      const openNode = (foundNode, cursor) => {
        if(foundNode && foundNode.is_category){
          foundNode.open=true;
          cursor = foundNode.categories;
        }
        return cursor;
      }
      treeExplorer(state, path, openNode);
    },
    [TOGGLE_NODE_OPEN](state, targetPath){
      const toggleNode = (foundNode, cursor) => {
        if(foundNode && foundNode.path==targetPath)
          foundNode.open = !foundNode.open;
        else if(foundNode && foundNode.is_category)
          cursor = foundNode.categories;
        return  cursor;
      }
      treeExplorer(state, targetPath, toggleNode);
    }
  },
}

// tree explorer that takes a function as an argument => used for mutations
function treeExplorer(state, path, f) {
  let parents = path.split('/');
  parents.splice(0, 1);
  let cursor = state.tree;
  path="";
  parents.forEach(function(val){
    path += "/" + val;
    let foundNode = cursor.find(item => item.path && item.path === path);
    cursor = f(foundNode, cursor);
  })
}
