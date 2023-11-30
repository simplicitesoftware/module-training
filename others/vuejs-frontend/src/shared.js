export default {
    callSearchService: function (simpliciteInstanceUrl, simpliciteToken, inputValue, lang, filters, pit) {
        return new Promise((res, rej) => {
            const headers = new Headers();
            headers.append("Authorization", simpliciteToken);
            headers.append("Content-Type", "application/json");

            const requestOptions = {
                method: 'GET',
                headers: headers,
                redirect: 'follow'
            };
            const urlParams = new URLSearchParams({
                query: inputValue,
                lang: lang,
                filters: filters,
                pit: pit
            });
            fetch(simpliciteInstanceUrl + "/api/ext/TrnSearchService/?"+urlParams, requestOptions)
                .then(response => response.json())
                .then((json) => {
                    if (json.length > 0) {
                        res(json);
                    }
                })
                .catch((error) => {
                    console.log(error)
                    rej([])
                })
                .finally(() => {
                    res([]);
                })
        })
    },
    getPIT: function (simpliciteInstanceUrl, simpliciteToken) {
        return new Promise((res, rej) => {
            const headers = new Headers();
            headers.append("Authorization", simpliciteToken);
            headers.append("Content-Type", "application/json");

            const requestOptions = {
                method: 'GET',
                headers: headers,
                redirect: 'follow'
            };
            const urlParams = new URLSearchParams({
                actionType: "getPIT"
            });
            fetch(simpliciteInstanceUrl + "/api/ext/TrnSearchService/?"+urlParams, requestOptions)
                .then(response => response.json())
                .then((json) => {
                    if(json.id) {
                        res(json.id);
                    } else {
                        res(null);
                    }
                })
                .catch((error) => {
                    console.log(error)
                    rej(null)
                })
                .finally(() => {
                    res(null);
                })
        })
    },
    deletePIT: function(simpliciteInstanceUrl, simpliciteToken, pit) {
        return new Promise((res, rej) => {
            const headers = new Headers();
            headers.append("Authorization", simpliciteToken);

            const requestOptions = {
                method: 'DELETE',
                headers: headers,
                redirect: 'follow'
            };
            const urlParams = new URLSearchParams({
                pit: pit
            });
            fetch(simpliciteInstanceUrl + "/api/ext/TrnSearchService/?"+urlParams, requestOptions)
                .then(response => response.json())
                .then((json) => {
                    console.log(json);
                    res("jaj");
                })
                .catch((error) => {
                    console.log(error)
                    rej(null)
                })
                .finally(() => {
                    res(null);
                })
        })
    }
}