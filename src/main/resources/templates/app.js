function todoApp() {
    return {
        todos: [],
        newTodo: '',
        filter: '',
        page: 'home',
        alert: '',
        selectedTodo: null,

        initTodos(initial) {
            this.todos = initial;
        },

        async addTodo() {
            if (!this.newTodo.trim()) return;
            const response = await fetch('/add', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                body: new URLSearchParams({todo: this.newTodo})
            });
            const todo = await response.json();
            this.todos.push(todo);
            this.alert = "Todo added successfully!";
            this.newTodo = '';
            setTimeout(() => this.alert = '', 2000);
        },

        async deleteTodo(id) {
            await fetch(`/delete/${id}`, {method: 'DELETE'});
            this.todos = this.todos.filter(t => t.id !== id);
            this.alert = "Todo deleted!";
            setTimeout(() => this.alert = '', 2000);
        },

        filteredTodos() {
            if (!this.filter) return this.todos;
            return this.todos.filter(t => t.text.toLowerCase().includes(this.filter.toLowerCase()));
        },

        openModal(todo) {
            this.selectedTodo = todo;
        },

        closeModal() {
            this.selectedTodo = null;
        }
    }
}
