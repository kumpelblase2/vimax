export function canBeDisplayedAsText(type) {
    switch(type) {
        case 'TEXT':
        case 'NUMBER':
        case 'RANGE':
        case 'BOOLEAN':
        case 'SELECTION':
            return true;
        default:
            return false;
    }
}

export function isSortable(type) {
    switch(type) {
        case 'TEXT':
        case 'NUMBER':
        case 'RANGE':
        case 'BOOLEAN':
        case 'DURATION':
            return true;
        default:
            return false;
    }
}

export function toDisplayValue(type, value) {
    if(value != null) {
        switch(type) {
            case 'TEXT':
            case 'NUMBER':
            case 'RANGE':
                return value.toString();
            case 'BOOLEAN':
                return value ? 'Yes' : 'No';
            case 'SELECTION':
                return value.name.toString();
            default:
                throw `No text representation for ${type}.`;
        }
    } else {
        return "";
    }
}
