

export default {
    highlightedContent: function(content, inputValue) {
        return content.replaceAll(
            inputValue,
            "<em>" + inputValue + "</em>"
        );
    },
    truncateContent: function(content, index) {
        if (
            content[index] === " " ||
            content[index] === "." ||
            content[index] === "?" ||
            content[index] === ";"
        ) {
            return content.substring(0, index) + " [...]";
        } else {
            return this.truncateContent(content, index + 1);
        }
    }
}