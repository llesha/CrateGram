var invalid = new Set()
var taskInvalid = new Set()
var valid = new Set()
var taskValid = new Set()
var highlighted

export function clearValid() {
    let grids = document.getElementsByClassName("status-grid")
    let saved = grids[0].getElementsByClassName("grid-cell")[0]
    grids[0].innerHTML = saved.outerHTML
    grids[0].getElementsByClassName("grid-cell")[0].onclick = clearValid
    valid.clear()
    taskValid.clear()
}

export function clearInvalid() {
    let grids = document.getElementsByClassName("status-grid")
    let saved = grids[1].getElementsByClassName("grid-cell")[0]
    grids[1].innerHTML = saved.outerHTML
    grids[1].getElementsByClassName("grid-cell")[0].onclick = clearInvalid
    invalid.clear()
    taskInvalid.clear()
}

export function addValueToTable(isMyGrammarValid, isTaskGrammarValid, index, value) {
    let grammarType = document.getElementById("grammar-type").textContent
    if (grammarType == "my grammar") {
        if (valid.has(value) || invalid.has(value))
            return
        _addToTable(isMyGrammarValid, "M ", value)
    }
    if (grammarType == "both grammars") {
        findAndRemoveCellByValue(value)
        if (isTaskGrammarValid == isMyGrammarValid)
            _addToTable(isMyGrammarValid, "TM", value)
        else {
            _addToTable(isTaskGrammarValid, "T ", value)
            _addToTable(isMyGrammarValid, "M ", value)
        }
    }
    else if (grammarType == "task grammar") {
        if (taskValid.has(value) || taskInvalid.has(value))
            return
        _addToTable(isTaskGrammarValid, "T ", value)
    }
}

/**
 * 
 * @param {boolean} isValid if true, add to left column
 * @param {string} type B: for both task and myGrammar, T: for task 
 * @param {string} value value of cell
 */
function _addToTable(isValid, type, value) {
    let grids = document.getElementsByClassName("status-grid")
    let newCell = document.createElement("div")
    newCell.classList.add("grid-cell")
    newCell.innerHTML = `<svg class="hidden" xmlns="http://www.w3.org/2000/svg" height="1em" viewBox="0 0 384 512">
    <!--! Font Awesome Free 6.4.0 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license (Commercial License) Copyright 2023 Fonticons, Inc. -->
    <path d="M342.6 150.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192 210.7 86.6 105.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L146.7 256 41.4 361.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192 301.3 297.4 406.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.3 256 342.6 150.6z"/></svg>
    <!--<i class="hidden fa-regular fa-clipboard to-left2"></i> -->
    <span style="margin-left: 0.3em; white-space: pre">  </span>
    <p class="grid-cell-content" style="display:inline">${value}</p>`
    if (isValid) {
        newCell.classList.add("grid-left")
        grids[0].appendChild(newCell)
        newCell = grids[0].lastElementChild
    } else {
        newCell.classList.add("grid-right")
        grids[1].appendChild(newCell)
        newCell = grids[1].lastElementChild
    }
    if (type == "T " || type == "TM") {
        if (isValid) { taskValid.add(value) }
        else {
            taskInvalid.add(value)
        }
    }
    if (type == "TM" || type != "T ") {
        valid.add(value)
        newCell.onmouseenter = () => {
            let parsed = window.myGrammar.parse(value)
            if (parsed[0]) {
                let astNode = window.myGrammar.getAst()
                window.ast.setValue(JSON.stringify(JSON.parse(astNode.toJson()), null, 2))
                if (highlighted != null && highlighted != newCell) {
                    highlighted.classList.remove("highlighted")
                }
                highlighted = newCell
                highlighted.classList.add("highlighted")
            }
        }
    } else {
        invalid.add(value)
    }
    newCell.getElementsByTagName("span")[0].textContent = type
    newCell.getElementsByTagName("svg")[0].onclick = () => { removeCell(newCell) }
    //newCell.getElementsByTagName("i")[1].onclick = () => { copyText(newCell.getElementsByTagName("span")[0]) }
}

function findAndRemoveCellByValue(text) {
    let grids = document.getElementsByClassName("status-grid")
    for (let grid of grids)
        for (let element of grid.children) {
            let textElement = element.getElementsByTagName("p")[0]
            if (textElement != null && textElement.textContent == text) {
                removeCell(element)
                return
            }
        }
}

function removeCell(element) {
    let type = element.getElementsByTagName("span")[0]
    if (type != "  ") {
        taskValid.delete(element.getElementsByTagName("p")[0].textContent)
        taskInvalid.delete(element.getElementsByTagName("p")[0].textContent)
    }
    if (type != "T ") {
        valid.delete(element.getElementsByTagName("p")[0].textContent)
        invalid.delete(element.getElementsByTagName("p")[0].textContent)
    }
    element.parentElement.removeChild(element)
}

function copyText(element) {
    navigator.clipboard.writeText(element.innerText);
}

export function moveCells() {
    let grids = document.getElementsByClassName("status-grid")
    _moveFromOneGrid(grids[0], (bool) => { return !bool }, (cell, value) => {
        grids[1].appendChild(cell)
        valid.delete(value)
        invalid.add(value)
    })
    _moveFromOneGrid(grids[1], (bool) => { return bool }, (cell, value) => {
        grids[0].appendChild(cell)
        taskValid.delete(value)
        taskInvalid.add(value)
    })
    if (window.currentGrammar != "playground")
        mergeCells(grids)
}

/**
 * Only `myGrammar` texts can be moved
 * @param {Element} grid of valid or invalid texts
 * @param {boolean} shouldMove if current text should move to another grid
 * @param {} move move function
 */
function _moveFromOneGrid(grid, shouldMove, move) {
    let gridChildren = _filterGridChildren(grid)
    for (const cell of gridChildren) {
        let type = cell.getElementsByTagName("span")[0].textContent
        let value = cell.getElementsByTagName("p")[0].textContent
        if (type == "TM") {
            let myStatus = window.myGrammar.hasGrammar() ? window.myGrammar.parse(value) : [false, 0]

            if (shouldMove(myStatus[0]))
                _moveCopy(cell, myStatus, shouldMove)
        } else if (type == "M ") {
            let myStatus = window.myGrammar.hasGrammar() ? window.myGrammar.parse(value) : [false, 0]
            if (shouldMove(myStatus[0]))
                move(cell, value)
        }
    }
}

function _moveCopy(prevValid, myStatus, shouldMove) {
    let copy = prevValid.cloneNode(true)
    let stayedAndCopied = shouldMove(myStatus[0]) ? ["T ", "M "] : ["M ", "T "]
    prevValid.getElementsByTagName("span")[0].textContent = stayedAndCopied[0]
    copy.getElementsByTagName("span")[0].textContent = stayedAndCopied[1]
    move(copy, value)
}

function mergeCells(grids) {
    _mergeGrid(grids[0])
    _mergeGrid(grids[1])
}

function _mergeGrid(grid) {
    let taskValues = {}
    let myGrammarValues = {}
    let gridChildren = _filterGridChildren(grid)
    for (const cell of gridChildren) {
        let type = cell.getElementsByTagName("span")[0].textContent
        let value = cell.getElementsByTagName("p")[0].textContent
        if (type == "T ") {
            if (myGrammarValues[value] != null) {
                grid.removeChild(myGrammarValues[value])
                cell.getElementsByTagName("span")[0].textContent = "TM"
            }
            else
                taskValues[value] = cell
        }
        else if (type == "M ") {
            if (taskValues[value]) {
                grid.removeChild(taskValues[value])
                cell.getElementsByTagName("span")[0].textContent = "TM"
            }
            else
                myGrammarValues[value] = cell
        }
    }
}

function _filterGridChildren(grid) {
    return Array.from(grid.getElementsByClassName("grid-cell"))
        .filter(e => e.getElementsByTagName("span").length > 0)
}
