export const activitiesService = {
    getActivities,
    getAllActivities,
    addActivities,
    deleteActivities
};

async function getActivities(id) {
    const response = await fetch(`/api/activities/${id}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response.json();
}
async function getAllActivities() {
    const response = await fetch('/api/activities', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return await response.json();
}

async function addActivities(data) {
    const response = await fetch('/api/activities', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    });
    return response;
}
async function deleteActivities(id) {
    const response = await fetch(`/api/activities/${id}`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    });
    return response.json();
}

