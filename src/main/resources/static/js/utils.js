function showElements(elements) {
    elements.forEach(function(element) {
        element.removeClass("hidden");
    })
}

function hideElements(elements) {
    elements.forEach(function(element) {
        element.addClass("hidden");
    })
}

function toggleElements(elementsToShow, elementsToHide) {
    showElements(elementsToShow);
    hideElements(elementsToHide);
}
