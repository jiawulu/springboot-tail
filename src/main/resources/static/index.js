(function () {
    var maxlines = 300;
    var interval = 100;
    var errorTimes = 0;

    var wsUri = "ws://localhost:8080/tail";
    var output;

    function connect() {
        websocket = new WebSocket(wsUri);
        websocket.onmessage = function (evt) {
            onMessage(evt)
        };
    }

    function onMessage(evt) {
        lines.push(evt.data)
    }

    var lines = [];

    function addLines(message) {
        lines.push(message);
    }

    function updateHtml() {
        var tmp = lines;
        lines = [];

        if (tmp.length == 0) {
            return;
        }

        var pres = '';
        for (var i = 0; i < tmp.length; i++) {
            pres += ("<pre>" + tmp[i] + "</pre>");
        }

        var child = $("#out pre");
        var start = child.length + tmp.length - maxlines;
        if (start > 0) {
            child.slice(0, start).remove()
        }

        $("#out").append(pres);
        window.scrollTo(0, document.body.scrollHeight);
    }

    connect();
    window.setInterval(function () {
        // console.log("update html by interval 1000")
        updateHtml();
    }, interval);

})();