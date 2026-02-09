package com.backend.coapp.repository;

import com.backend.coapp.model.document.ApplicationModel;
import com.backend.coapp.model.enumeration.ApplicationStatusEnum;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/** Repository to interact with job applications in the database */
@Repository
public interface ApplicationRepository extends MongoRepository<ApplicationModel, String> {

  /**
   * Finds all applications for a specific user.
   *
   * @param userId The user's ID
   * @return A list of the user's applications
   */
  List<ApplicationModel> findByUserId(String userId);

  /**
   * Finds all applications for a specific company.
   *
   * @param companyId The company's ID
   * @return A list of applications for that company
   */
  List<ApplicationModel> findByCompanyId(String companyId);

  /**
   * Finds applications by user and status
   *
   * @param userId The user's ID
   * @param status The status to filter by (e.g., INTERVIEWING)
   * @return A filtered list of applications
   */
  List<ApplicationModel> findByUserIdAndStatus(String userId, ApplicationStatusEnum status);
}
