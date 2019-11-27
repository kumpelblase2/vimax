import { INDEX, SORTING } from "./views";

export function supportsSearch(view) {
    return view === INDEX;
}

export function supportsOrder(view) {
    return view === INDEX;
}

export function supportsMetadataFilter(view) {
    return view === INDEX || view === SORTING;
}

export function supportsPlaylist(view) {
    return view === INDEX;
}
