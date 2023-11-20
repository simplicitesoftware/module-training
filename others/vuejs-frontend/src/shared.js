export default {
    callSearchService: function (simpliciteInstanceUrl, simpliciteToken, inputValue, lang, filters) {
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
                filters: filters
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
    }
}