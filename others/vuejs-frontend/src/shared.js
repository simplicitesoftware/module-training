export default {
    callSearchService: function (simpliciteInstanceUrl, simpliciteToken, inputValue, lang, filters, page) {
        return new Promise((res ,rej) => {
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
                page: page
            });
            fetch(simpliciteInstanceUrl + "/api/ext/TrnSearchService/?"+urlParams, requestOptions)
                .then(response => response.json())
                .then((json) => {
                    res(json);
                })
                .catch((error) => {
                    console.log(error);
                    rej([]);
                })
                .finally(() => {
                    res([]);
                })
        })
    }
}