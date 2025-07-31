package main

import (
    "fmt"
    "log"
    "math/rand"
    "sync"
    "time"
)

// Defining the Task struct, representing each unit of work with an ID
type Task struct {
    ID int
}

// Creating a worker function to keep processing incoming tasks from the taskChan
// and sending results to the resultChan while decrementing the WaitGroup when done
func worker(id int, taskChan <-chan Task, resultChan chan<- string, wg *sync.WaitGroup) {
    defer wg.Done()

    // Continuously receiving tasks from the channel until it's closed
    for task := range taskChan {
        log.Printf("Worker %d starting task %d\n", id, task.ID)

        // Simulating task processing by sleeping for 100-500 milliseconds
        time.Sleep(time.Millisecond * time.Duration(rand.Intn(400)+100))

        // Formatting and sending the result string to the result channel
        result := fmt.Sprintf("Task %d processed by Worker %d", task.ID, id)
        resultChan <- result

        log.Printf("Worker %d finishing task %d\n", id, task.ID)
    }
}

func main() {
    // Declaring number of workers and tasks
    numWorkers := 3
    numTasks := 10

    // Creating a buffered task channel to hold all tasks
    taskChan := make(chan Task, numTasks)

    // Creating a buffered result channel to store all processed results
    resultChan := make(chan string, numTasks)

    // Declaring a WaitGroup to wait for all goroutines to finish
    var wg sync.WaitGroup

    // Starting multiple worker goroutines and adding them to the WaitGroup
    for i := 1; i <= numWorkers; i++ {
        wg.Add(1)                     
        go worker(i, taskChan, resultChan, &wg) 
    }

    // Sending tasks to the task channel
    for i := 1; i <= numTasks; i++ {
        taskChan <- Task{ID: i} 
    }

    // Closing the task channel after all tasks are sent
    close(taskChan)

    // Waiting for all workers to complete their execution
    wg.Wait()

    // Closing the result channel after all results are written
    close(resultChan)

    // Printing all results collected from the result channel
    fmt.Println("\n Results ")
    for res := range resultChan {
        fmt.Println(res)
    }
}
