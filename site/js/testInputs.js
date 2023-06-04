var invalid = new Set()
var taskInvalid = new Set()
var asts = {}
var taskValid = new Set()
var highlighted

export function clearValid() {
    let grids = document.getElementsByClassName("status-grid")
    let saved = grids[0].getElementsByClassName("grid-cell")[0]
    grids[0].innerHTML = saved.outerHTML
    grids[0].getElementsByClassName("grid-cell")[0].onclick = clearValid
    asts = {}
}

export function clearInvalid() {
    let grids = document.getElementsByClassName("status-grid")
    let saved = grids[1].getElementsByClassName("grid-cell")[0]
    grids[1].innerHTML = saved.outerHTML
    grids[1].getElementsByClassName("grid-cell")[0].onclick = clearInvalid
    invalid.clear()
}

export function addValueToTable(isMyGrammarValid, index, value) {
    let grammarType = document.getElementById("grammar-type")

    if (grammarType.textContent == "my grammar") {
        if (asts[value] != null || invalid.has(value))
            return
        _addToTable(isMyGrammarValid, "M", value)
    }
    let taskGrammarStatus = window.Interpreter.parse(value)
    if (grammarType.textContent == "both grammars") {
        findAndRemoveCellByValue(value)
        if (taskGrammarStatus[0] == isMyGrammarValid)
            _addToTable(isMyGrammarValid, "B", value)
        else {
            _addToTable()
        }
    }
    else if (grammarType.textContent == "task grammar") {
        if (taskValid.has(value) || taskInvalid.has(value))
            return
        _addToTable(taskGrammarStatus, "T", value)
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
    newCell.innerHTML = `<i class="hidden fa-solid fa-xmark to-left"></i>
    <!--<i class="hidden fa-regular fa-clipboard to-left2"></i> -->
    <span style="margin-left: 0.3em">&nbsp</span>
    <p class="grid-cell-content" style="display:inline">${value}</p>`
    if (isValid) {
        newCell.classList.add("grid-left")
        grids[0].appendChild(newCell)
    } else {
        newCell.classList.add("grid-right")
        grids[1].appendChild(newCell)
    }
    if (type == "T" || type == "B") {
        if (isValid) taskValid.add(value)
        else
            taskInvalid.add(value)
        taskAsts[value] = newCell.getElementsByTagName("span")[0].textContent = type
    } else if (type == "B" || type != "T") {
        if (isValid) {
            let astNode = window.myGrammar.getAst()
            asts[value] = JSON.stringify(JSON.parse(astNode.toJson()), null, 2)
            newCell.onmouseenter = () => {
                window.ast.setValue(asts[value])
                if (highlighted != null && highlighted != newCell) {
                    highlighted.classList.remove("highlighted")
                }
                highlighted = newCell
                highlighted.classList.add("highlighted")
            }
        } else {
            invalid.add(value)
        }
    }
    newCell.getElementsByTagName("i")[0].onclick = () => { removeCell(newCell) }
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
    if (type != "&nbsp") {
        taskAsts.delete(element.getElementsByTagName("p")[0].textContent)
        taskInvalid.delete(element.getElementsByTagName("p")[0].textContent)
    }
    if (type != "T") {
        delete asts[element.getElementsByTagName("p")[0].textContent]
        invalid.delete(element.getElementsByTagName("p")[0].textContent)
    }
    element.parentElement.removeChild(element)
}

function copyText(element) {
    navigator.clipboard.writeText(element.innerText);
}
