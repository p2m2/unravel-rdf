const log = document.getElementById("log");

const originalConsoleLog = console.log;
const originalConsoleError = console.error;


function formatMessage(value) {
    if (typeof value === "object") {
        return JSON.stringify(value, null, 2);
    }

    return String(value);
}


console.log = (...args) => {
    originalConsoleLog(...args);

    log.textContent += args
        .map(formatMessage)
        .join(" ") + "\n";
};


console.error = (...args) => {
    originalConsoleError(...args);

    log.textContent += "\nERROR:\n";
    log.textContent += args
        .map(formatMessage)
        .join(" ") + "\n";
};