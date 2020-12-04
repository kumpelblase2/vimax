export function areSame(first, second) {
    if(first.length !== second.length) {
        return false;
    }

    for(let i = 0; i < first.length; i++) {
        if(first[i] !== second[i]) {
            return false;
        }
    }

    return true;
}

export function removeFromArray(array, item) {
    const index = array.indexOf(item);
    if(index >= 0) {
        array.splice(index, 1);
    }
}

export function removeFromArrayWhere(array, func) {
    const index = array.findIndex(func);
    if(index >= 0) {
        array.splice(index, 1);
    }
}
