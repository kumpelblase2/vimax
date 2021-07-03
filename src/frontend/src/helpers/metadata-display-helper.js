export function canBeDisplayedAsText(type) {
    switch(type) {
        case 'TEXT':
        case 'NUMBER':
        case 'FLOAT':
        case 'RANGE':
        case 'BOOLEAN':
        case 'SELECTION':
        case 'DURATION':
            return true;
        default:
            return false;
    }
}

export function isSortable(type) {
    switch(type) {
        case 'TEXT':
        case 'NUMBER':
        case 'FLOAT':
        case 'RANGE':
        case 'BOOLEAN':
        case 'DURATION':
            return true;
        default:
            return false;
    }
}

export function toDisplayValue(metadata, value) {
    if(value != null) {
        switch(metadata.type) {
            case 'TEXT':
            case 'NUMBER':
            case 'RANGE':
                return value.toString();
            case 'FLOAT':
                return (Math.round(value * 100) / 100).toString();
            case 'BOOLEAN':
                return value ? 'Yes' : 'No';
            case 'SELECTION':
                if(value.id != null) {
                    return value.name;
                }
                const selected = metadata.options.values.find(v => v.id === value);
                return selected.name.toString();
            case 'DURATION':
                return durationToString(value);
            default:
                throw `No text representation for ${metadata.type}.`;
        }
    } else {
        return "";
    }
}

export function durationToString(value) {
    const parsedDuration = parseDurationString(value);
    const daysPart = parsedDuration.days > 0 ? parsedDuration.days + "d " : "";
    const hourPart = parsedDuration.hours > 0 ? parsedDuration.hours + "h " : "";
    const minutesPart = parsedDuration.minutes > 0 ? parsedDuration.minutes + "m " : "";
    const secondsPart = parsedDuration.seconds > 0 || (parsedDuration.days + parsedDuration.hours + parsedDuration.minutes === 0) ? parsedDuration.seconds + "s " : "";
    return (daysPart + hourPart + minutesPart + secondsPart).trim();
}

export function parseDurationString(durationString) {
    const stringPattern = /^PT(?:(\d+)D)?(?:(\d+)H)?(?:(\d+)M)?(?:(\d+(?:\.\d{1,3})?)S)?$/;
    const stringParts = stringPattern.exec(durationString);

    const days = stringParts[1] === undefined ? 0 : stringParts[1] * 1;
    const hours = stringParts[2] === undefined ? 0 : stringParts[2] * 1;
    const minutes = stringParts[3] === undefined ? 0 : stringParts[3] * 1;
    const seconds = stringParts[4] === undefined ? 0 : stringParts[4] * 1;
    return {
        days,
        hours,
        minutes,
        seconds
    };
}
