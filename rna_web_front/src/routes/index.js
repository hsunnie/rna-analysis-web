import React from "react";
import {Routes, Route} from 'react-router-dom';

const AllRoutes = () => {
    return(
        <Routes>
            <Route path="/" element={<div>Home</div>} />
        </Routes>
    );
}

export default AllRoutes