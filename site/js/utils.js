export function addOrRemove(array, element) {
    array = Array.from(array)
    const index = array.indexOf(element)
    if (index == -1) {
        array.push(element)
    } else {
        array.splice(index, 1)
    }

    return array
}

export function addValueToTable(isSuccess, index, value) {
    let grids = document.getElementsByClassName("status-grid")
    let newCell = document.createElement("div")
    newCell.classList.add("grid-cell")
    newCell.innerHTML = `<i class="hidden fa-solid fa-xmark to-left"></i>
    <i class="hidden fa-regular fa-clipboard to-left2"></i>
    <span style="margin-left:2.2em">${value}</span>`
    if (isSuccess) {
        newCell.classList.add("grid-left")
        grids[0].appendChild(newCell)
    } else {
        newCell.classList.add("grid-right")
        grids[1].appendChild(newCell)
    }
    newCell.getElementsByTagName("i")[0].onclick = () => { removeCell(newCell) }
    newCell.getElementsByTagName("i")[1].onclick = () => { copyText(newCell.getElementsByTagName("span")[0]) }
}

function removeCell(element) {
    console.log(element)
    element.parentElement.removeChild(element)
}

function copyText(element) {
    navigator.clipboard.writeText(element.innerText);
}
