import React from "react";
import MessageBusMessages from "./messages/MessageBusMessages";
import Routes from "../../../../util/ApiRoutes";
import TasksQueueTasks from "./tasks/TasksQueueTasks";

function MessagingPage() {

  return (
      <div>
        <div className="row">
          <div className="col-md-10">
            <nav aria-label="breadcrumb">
              <ol className="breadcrumb">
                <li className="breadcrumb-item"><a href={Routes.SYSTEM.CLUSTER.INDEX}>Cluster &amp; Nodes</a></li>
                <li className="breadcrumb-item active" aria-current="page">Messaging</li>
              </ol>
            </nav>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <h1>Cluster Messaging</h1>
          </div>
        </div>

        <div className="row mt-3">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <p className="mb-0">
                  This page provides real-time insights into inter-cluster communications. There is usually no need to
                  spend time here except when debugging something that went wrong, but you can feel free to dig around.
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <TasksQueueTasks />
          </div>
        </div>

        <div className="row">
          <div className="col-md-12">
            <MessageBusMessages />
          </div>
        </div>
      </div>
  )

}

export default MessagingPage;
