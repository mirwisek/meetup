items = [
    {
        voters: ["11", "22", "33"]
    },
    {
        voters: ["55", "66", "77"]
    }
]

var allVoters = []
items.forEach(i => {
    allVoters = allVoters.concat(i.voters)
})

Array.prototype.removeByValue = function(val) {
    for (let i = 0; i < this.length; i++) {
        if (this[i] === val) {
            this.splice(i, 1);
            i--;
        }
    }
    return this;
}

// console.log(allVoters)
voters = ["11", "22", "33"]
nVotes = ["11", "22", "33", "44"].removeByValue("33")

// console.log(voters.length)
console.log(nVotes.filter(n => !voters.includes(n)))
