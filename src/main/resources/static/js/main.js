document.addEventListener("DOMContentLoaded", () => {
    const main = document.getElementById("spa-content");

    async function loadPage(url, push = true) {
        const res = await fetch(url);
        const html = await res.text();
        const parser = new DOMParser();
        const doc = parser.parseFromString(html, "text/html");
        const newContent = doc.querySelector("main")?.innerHTML || html;
        main.innerHTML = newContent;
        if (push) history.pushState({ url }, "", url);
    }

    document.querySelectorAll(".spa-link").forEach(link => {
        link.addEventListener("click", e => {
            e.preventDefault();
            const url = link.getAttribute("href");
            if (url) loadPage(url);
        });
    });

    window.addEventListener("popstate", e => {
        if (e.state?.url) loadPage(e.state.url, false);
    });
});
