listView('Snapshots') {
  recurse(true)
  columns {
    jobName()
    status()
    buildButton()
  }
  jobs {
    regex('.*snapshot$')
  }
}

listView('Releases') {
  recurse(true)
  columns {
    jobName()
    status()
    buildButton()
  }
  jobs {
    regex('.*release$')
  }
}

listView('Merge Requests') {
  recurse(true)
  columns {
    jobName()
    status()
    buildButton()
  }
  jobs {
    regex('.*merge-request$')
  }
}
