package com.mycompany.myapp.service.impl;

import static org.elasticsearch.index.query.QueryBuilders.*;

import com.mycompany.myapp.domain.Task;
import com.mycompany.myapp.repository.TaskRepository;
import com.mycompany.myapp.repository.search.TaskSearchRepository;
import com.mycompany.myapp.service.TaskService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Task}.
 */
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final Logger log = LoggerFactory.getLogger(TaskServiceImpl.class);

    private final TaskRepository taskRepository;

    private final TaskSearchRepository taskSearchRepository;

    public TaskServiceImpl(TaskRepository taskRepository, TaskSearchRepository taskSearchRepository) {
        this.taskRepository = taskRepository;
        this.taskSearchRepository = taskSearchRepository;
    }

    @Override
    public Task save(Task task) {
        log.debug("Request to save Task : {}", task);
        Task result = taskRepository.save(task);
        taskSearchRepository.save(result);
        return result;
    }

    @Override
    public Optional<Task> partialUpdate(Task task) {
        log.debug("Request to partially update Task : {}", task);

        return taskRepository
            .findById(task.getId())
            .map(existingTask -> {
                if (task.getTitle() != null) {
                    existingTask.setTitle(task.getTitle());
                }
                if (task.getDescription() != null) {
                    existingTask.setDescription(task.getDescription());
                }

                return existingTask;
            })
            .map(taskRepository::save)
            .map(savedTask -> {
                taskSearchRepository.save(savedTask);

                return savedTask;
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> findAll() {
        log.debug("Request to get all Tasks");
        return taskRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Task> findOne(Long id) {
        log.debug("Request to get Task : {}", id);
        return taskRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Task : {}", id);
        taskRepository.deleteById(id);
        taskSearchRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Task> search(String query) {
        log.debug("Request to search Tasks for query {}", query);
        return StreamSupport.stream(taskSearchRepository.search(query).spliterator(), false).collect(Collectors.toList());
    }
}
